package com.example.thong.astthonggooglemap.parse;

import com.example.thong.astthonggooglemap.model.LocationSearch;

import java.util.ArrayList;

import retrofit.http.GET;
import retrofit.http.Query;

public interface ApiService {
    @GET("/v2/venues/search")
    ArrayList<LocationSearch> getLocationSearches(@Query("intent") String intent,
                                                  @Query("ll") String ll,
                                                  @Query("query") String query,
                                                  @Query("oauth_token") String token,
                                                  @Query("v") String v);
}
