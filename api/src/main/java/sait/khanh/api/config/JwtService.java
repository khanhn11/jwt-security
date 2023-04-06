package sait.khanh.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * JWT Validation Service
 * 
 * @author Khanh Nguyen
 */
@Service
public class JwtService {

    private static final String SECRET_KEY = "792F413F4428472B4B6250655368566D597133743677397A244326452948404D";

    /**
     * decode secret key
     * 
     * @return
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * get the user claims from the token
     * 
     * @param token
     * @return
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * extract a single claim is passed
     * 
     * @param <T>
     * @param token
     * @param claimsResolver
     * @return
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // extract all claims
        final Claims claims = extractAllClaims(token);
        // return list of claims
        return claimsResolver.apply(claims);
    }

    /**
     * extract username from jwt token
     * 
     * @param token
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * create token with user details only
     * 
     * @param userDetails
     * @return
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * create new jwt token from user credentials
     * 
     * @param extractClaims
     * @param userDetails
     * @return
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * check if the token is valid and not expired yet
     * 
     * @param token
     * @param userDetails
     * @return true if the username in token is valid and not expired, false
     *         otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {

        final String username = extractUsername(token); // get the username signed by the token

        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * check if the token is expired
     * 
     * @param token
     * @return
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * get the expiration date from the token
     * 
     * @param token
     * @return
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
