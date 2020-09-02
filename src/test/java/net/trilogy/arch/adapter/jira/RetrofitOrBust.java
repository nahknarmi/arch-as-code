package net.trilogy.arch.adapter.jira;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

import static java.lang.System.out;

public class RetrofitOrBust {
    public static void main(String[] args) throws IOException {
        // TODO: Hold onto this instance for global reuse
        final var retrofit = new Retrofit.Builder()
                .baseUrl("https://tw-trilogy.atlassian.net/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        // TODO: Hold onto this instance for global reuse; see above TODO
        final var jira = retrofit.create(RemoteJira.class);

        final var issue = jira.browseIssue("AAC-129").execute();
        out.println("issue response = " + issue);
    }
}

