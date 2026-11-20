
package com.vertexone.prod.security.dto.response;

public record LoginResponse(

        String accessToken,

        String tokenType,

        long expiresInSeconds

) {}
