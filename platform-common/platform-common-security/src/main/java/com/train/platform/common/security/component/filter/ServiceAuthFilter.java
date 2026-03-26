package com.train.platform.common.security.component.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class ServiceAuthFilter extends OncePerRequestFilter {
	private static final String URI_PREFIX = "/internal/api/";
	private static final String SERVICE_AUTH_HEADER = "X-Service-Key";
	private final String serviceSecret;

	public ServiceAuthFilter(String serviceSecret) {
		log.info("serviceSecret: {}", serviceSecret);
		this.serviceSecret = serviceSecret;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!request.getRequestURI().startsWith(URI_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}

		String key = request.getHeader(SERVICE_AUTH_HEADER);
		if (key == null || key.isEmpty()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing service key");
			return;
		}

		// 安全比较（防止时序攻击）
		if (isSecretValid(key, serviceSecret)) {
			Authentication auth = new ServiceAuthentication(key);
			SecurityContextHolder.getContext().setAuthentication(auth);
			chain.doFilter(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid service key");
		}
	}

	private boolean isSecretValid(String candidate, String secret) {
		if (candidate.length() != secret.length()) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < candidate.length(); i++) {
			result |= candidate.charAt(i) ^ secret.charAt(i);
		}
		return result == 0;
	}

	public static class ServiceAuthentication implements Authentication {
		private final String serviceKey;
		private boolean authenticated = true;

		public ServiceAuthentication(String serviceKey) {
			this.serviceKey = serviceKey;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return Collections.emptyList();
		}

		@Override
		public Object getCredentials() {
			return serviceKey;
		}

		@Override
		public Object getDetails() {
			return null;
		}

		@Override
		public Object getPrincipal() {
			return serviceKey;
		}

		@Override
		public boolean isAuthenticated() {
			return authenticated;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) {
			this.authenticated = isAuthenticated;
		}

		@Override
		public String getName() {
			return serviceKey; // 服务密钥作为名称
		}
	}
}
