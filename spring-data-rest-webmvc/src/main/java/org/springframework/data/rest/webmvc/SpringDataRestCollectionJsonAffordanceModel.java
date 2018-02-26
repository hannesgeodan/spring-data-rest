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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModelProperty;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.collectionjson.CollectionJsonData;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * @author Greg Turnquist
 */
@Slf4j
public class SpringDataRestCollectionJsonAffordanceModel implements SpringDataRestAffordanceModel {

	private static final List<HttpMethod> METHODS_FOR_INPUT_DETECTION = Arrays.asList(HttpMethod.POST, HttpMethod.PUT,
		HttpMethod.PATCH);

	private final Affordance affordance;
	private final @Getter Link link;
	private final @Getter List<AffordanceModelProperty> inputProperties;
	private final @Getter List<AffordanceModelProperty> queryProperties;

	public SpringDataRestCollectionJsonAffordanceModel(Affordance affordance, Class<?> domainType, Link link) {

		this.affordance = affordance;
		this.link = link;
		this.inputProperties = determineAffordanceInputs(domainType);
		this.queryProperties = determineQueryProperties();
	}

	public SpringDataRestCollectionJsonAffordanceModel(Affordance affordance, Object instance, Link link) {
		this(affordance, instance.getClass(), link);
	}
	
	/**
	 * The media types this is a model for. Can be multiple ones as often media types come in different flavors like an
	 * XML and JSON one and in simple cases a single model might serve them all.
	 *
	 * @return will never be {@literal null}.
	 */
	@Override
	public Collection<MediaType> getMediaTypes() {
		return Collections.singleton(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * Look at the entity's type to descide the {@link Affordance}'s properties.
	 */
	private List<AffordanceModelProperty> determineAffordanceInputs(Class<?> parameterType) {

		if (!METHODS_FOR_INPUT_DETECTION.contains(affordance.getHttpMethod())) {
			return Collections.emptyList();
		}

		Map<String, Class<?>> properties = new TreeMap<>();

		LOG.debug("\tEntity type: " + parameterType.getCanonicalName() + "(");

		for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(parameterType)) {

			if (!descriptor.getName().equals("class")) {

				LOG.debug("\t\t" + descriptor.getPropertyType().getCanonicalName() + " " + descriptor.getName());
				properties.put(descriptor.getName(), descriptor.getPropertyType());
			}
		}

		LOG.debug(")");
		LOG.debug("Assembled " + this.toString());

		return properties.entrySet().stream()
			.map(Map.Entry::getKey)
			.map(CollectionJsonData::named)
			.map(collectionJsonData -> (AffordanceModelProperty) collectionJsonData)
			.collect(Collectors.toList());
	}

	/**
	 * Transform a list of {@link QueryParameter}s into a list of {@link CollectionJsonData} objects.
	 *
	 * @return
	 */
	private List<AffordanceModelProperty> determineQueryProperties() {

		if (!this.affordance.getHttpMethod().equals(HttpMethod.GET)) {
			return Collections.emptyList();
		}

		return this.affordance.getQueryMethodParameters().stream()
			.map(queryProperty -> CollectionJsonData.named(queryProperty.getName()).withValue(""))
			.collect(Collectors.toList());
	}

}