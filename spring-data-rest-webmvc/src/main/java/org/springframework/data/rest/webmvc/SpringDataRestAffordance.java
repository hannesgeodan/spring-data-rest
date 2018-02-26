/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.webmvc;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

/**
 * Spring Data REST implementation of the {@link Affordance} API.
 * 
 * @author Greg Turnquist
 */
@Value
@RequiredArgsConstructor
public class SpringDataRestAffordance implements Affordance {

	private final HttpMethod httpMethod;
	private final String name;
	private Map<MediaType, AffordanceModel> affordanceModels = new HashMap<>();

	/**
	 * Get a listing of {@link MethodParameter}s.
	 *
	 * @return
	 */
	@Override
	public List<MethodParameter> getInputMethodParameters() {
		return null;
	}

	/**
	 * Get a listing of {@link QueryParameter}s.
	 *
	 * @return
	 */
	@Override
	public List<QueryParameter> getQueryMethodParameters() {
		return null;
	}

	/**
	 * Look up the {@link AffordanceModel} for the requested {@link MediaType}.
	 *
	 * @param mediaType
	 * @return
	 */
	@Override
	public <T extends AffordanceModel> T getAffordanceModel(MediaType mediaType) {
		return (T) this.affordanceModels.get(mediaType);
	}

	public void addAffordanceModel(AffordanceModel affordanceModel) {

		Assert.notNull(affordanceModel, "Affordance model must not be null!");

		for (MediaType mediaType : affordanceModel.getMediaTypes()) {
			this.affordanceModels.put(mediaType, affordanceModel);
		}
	}
}
