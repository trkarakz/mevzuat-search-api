package com.evrim.mevzuat.search.api.config;

import org.springframework.stereotype.Service;

import com.evrim.common.dto.security.AuthResultWithCompanyDto;
import com.evrim.common.security.AuthenticationService;
import com.evrim.common.util.SecurityContextUtil;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	@Override
	public AuthResultWithCompanyDto getAuthenticatedUser() {
		return SecurityContextUtil.getAuthenticatedUser();
	}

}
