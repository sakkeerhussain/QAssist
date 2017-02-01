package com.qburst.plugin.android.retrofit;

/**
 * Created by sakkeer on 18/01/17.
 */
public class Constants {
    public static final String DEPENDENCY_RETROFIT = "com.squareup.retrofit2:retrofit:2.1.0";
    public static final String DEPENDENCY_RETROFIT_GSON = "com.squareup.retrofit2:converter-gson:2.1.0";


    public static final String GET_INSTANCE_METHOD = "public static APIService getInstance(final Context context) {\n" +
            "\n" +
            "        APIService service;\n" +
            "\n" +
            "        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();\n" +
            "        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);\n" +
            "        Interceptor headerInterceptor = new Interceptor() {\n" +
            "            @Override\n" +
            "            public Response intercept(Chain chain) throws IOException {\n" +
            "                Request.Builder requestBuilder = chain.request().newBuilder()\n" +
            "                        .addHeader(\"Content-Type\", \"Application/json\");\n" +
            "                return chain.proceed(requestBuilder.build());\n" +
            "            }\n" +
            "        };\n" +
            "\n" +
            "        OkHttpClient client = new OkHttpClient.Builder()\n" +
            "                .addInterceptor(loggingInterceptor)\n" +
            "                .addInterceptor(headerInterceptor)\n" +
            "                .readTimeout(2, TimeUnit.MINUTES)\n" +
            "                .writeTimeout(2, TimeUnit.MINUTES)\n" +
            "                .build();\n" +
            "\n" +
            "        Retrofit retrofit = new Retrofit.Builder()\n" +
            "                .baseUrl(BASE_URL)\n" +
            "                .client(client)\n" +
            "                .addConverterFactory(GsonConverterFactory.create())\n" +
            "                .build();\n" +
            "\n" +
            "        service = retrofit.create(APIService.class);\n" +
            "\n" +
            "        return service;\n" +
            "    }";
}
