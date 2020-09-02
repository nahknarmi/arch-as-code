package net.trilogy.arch.adapter.google;

import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.Body;
import com.google.api.services.docs.v1.model.Document;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class GoogleDocsApiInterfaceTest {
    private Docs mockedApi;
    private GoogleDocsApiInterface apiInterface;

    @Before
    public void setUp() {
        mockedApi = mock(Docs.class);
        apiInterface = new GoogleDocsApiInterface(mockedApi);
    }

    @Parameters({"", " ", " \n ", "https://docs.fake.com/document/d/"})
    @Test(expected = GoogleDocsApiInterface.InvalidUrlException.class)
    public void shouldRaiseExceptionOnEmptyUrl(String url) throws IOException {
        apiInterface.fetch(url);
    }

    @Parameters({
            "https://docs.fake.com/document/d/1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk/view | 1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk",
            "https://docs.fake.com/document/d/1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk/edit?usp=sharing | 1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk",
            "docs.fake.com/document/d/1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk/view | 1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk",
            "docs.fake.com/document/d/1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk | 1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk",
            "https://docs.fake.com/document/d/1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk | 1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk",
            "docs.fake.com/document/d/1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk | 1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk",
    })
    @Test
    public void shouldParseDocumentId(String url, String id) throws Exception {
        mockApiToReturn(new Document(), id);

        apiInterface.fetch(url);

        verify(mockedApi.documents()).get(id);
    }

    @Test
    public void shouldReturnDocument() throws Exception {
        String id = "1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk";
        String url = "https://docs.fake.com/document/d/" + id;
        final Document doc = new Document();
        doc.setBody(new Body());

        // given
        mockApiToReturn(doc, id);

        // when
        GoogleDocsApiInterface.Response output = apiInterface.fetch(url);

        // then
        assertThat(output.asDocument(), is(doc));
    }

    @Test
    public void shouldReturnsJson() throws Exception {
        String id = "1yTTKKPfZzf6Q6h4IBxT1u_-DrarilQnvpNCp6LRTlfk";
        String url = "https://docs.fake.com/document/d/" + id;
        final Document doc = new Document();
        doc.setBody(new Body());

        // given
        mockApiToReturn(doc, id);

        // when
        GoogleDocsApiInterface.Response output = apiInterface.fetch(url);

        // then
        assertThat(output.asJson().get("body").toString(), equalTo("{}"));
    }

    private void mockApiToReturn(Document toReturn, String given) throws IOException {
        var mockedDocuments = mock(Docs.Documents.class);
        when(mockedApi.documents()).thenReturn(mockedDocuments);

        var mockedGet = mock(Docs.Documents.Get.class);
        when(mockedDocuments.get(given)).thenReturn(mockedGet);

        when(mockedGet.execute()).thenReturn(toReturn);
    }
}
