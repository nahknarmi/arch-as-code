package net.trilogy.arch.adapter.jira;

import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

import static java.lang.System.out;

public class RetrofitOrBust {
    private static final String trilogyBaseUrl = "https://tw-trilogy.atlassian.net/";

    public static final RemoteJira REMOTE_JIRA = new Builder()
            .baseUrl(trilogyBaseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(RemoteJira.class);

    public static void main(final String... args) throws IOException {
        final var issue = REMOTE_JIRA.browseIssue("AAC-129").execute();

        out.println("issue response = " + issue);
    }
}

