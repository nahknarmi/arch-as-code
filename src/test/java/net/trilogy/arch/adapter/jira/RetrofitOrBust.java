package net.trilogy.arch.adapter.jira;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

import static java.lang.System.out;

public class RetrofitOrBust {
    public static void main(String[] args) throws IOException {
        final var retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        final var bob = retrofit.create(XTheBob.class);

        final var callX = bob.get().execute();
        out.println("callX status code = " + callX.code());
    }
}

