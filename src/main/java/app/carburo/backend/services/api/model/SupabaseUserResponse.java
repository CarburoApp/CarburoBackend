package app.carburo.backend.services.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupabaseUserResponse(
		String id,
		String aud,
		String role,
		String email,
		@JsonProperty("email_confirmed_at") String emailConfirmedAt,
		String phone,
		@JsonProperty("confirmed_at") String confirmedAt,
		@JsonProperty("last_sign_in_at") String lastSignInAt,
		@JsonProperty("created_at") String createdAt,
		@JsonProperty("updated_at") String updatedAt,
		@JsonProperty("is_anonymous") boolean isAnonymous,
		@JsonProperty("app_metadata") AppMetadata appMetadata,
		@JsonProperty("user_metadata") UserMetadata userMetadata,
		List<Identity> identities
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record AppMetadata(
			String provider,
			List<String> providers
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record UserMetadata(
			String email,
			@JsonProperty("email_verified") boolean emailVerified,
			@JsonProperty("phone_verified") boolean phoneVerified,
			String name,
			@JsonProperty("full_name") String fullName,
			String iss,
			String sub,
			@JsonProperty("avatar_url") String avatarUrl,
			String picture,
			@JsonProperty("provider_id") String providerId
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Identity(
			@JsonProperty("identity_id") String identityId,
			String id,
			@JsonProperty("user_id") String userId,
			@JsonProperty("identity_data") IdentityData identityData,
			String provider,
			@JsonProperty("last_sign_in_at") String lastSignInAt,
			@JsonProperty("created_at") String createdAt,
			@JsonProperty("updated_at") String updatedAt,
			String email
	) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record IdentityData(
			String email,
			@JsonProperty("email_verified") boolean emailVerified,
			@JsonProperty("phone_verified") boolean phoneVerified,
			String name,
			@JsonProperty("full_name") String fullName,
			String iss,
			String sub,
			@JsonProperty("avatar_url") String avatarUrl,
			String picture
	) {}
}