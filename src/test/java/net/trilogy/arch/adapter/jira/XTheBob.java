package net.trilogy.arch.adapter.jira;

import retrofit2.Call;
import retrofit2.http.GET;

public interface XTheBob {
    @GET("/suck-it-in")
    Call<X> get();
}
