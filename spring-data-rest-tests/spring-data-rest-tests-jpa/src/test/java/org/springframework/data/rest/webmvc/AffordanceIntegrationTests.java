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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.tests.AbstractWebIntegrationTests;
import org.springframework.data.rest.webmvc.jpa.JpaRepositoryConfig;
import org.springframework.data.rest.webmvc.jpa.TestDataPopulator;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Greg Turnquist
 */
@Transactional
@ContextConfiguration(classes = JpaRepositoryConfig.class)
public class AffordanceIntegrationTests extends AbstractWebIntegrationTests {

	HalLinkDiscoverer halLinkDiscoverer;

	HalFormsLinkDiscoverer halFormsLinkDiscoverer;

	@Autowired TestDataPopulator loader;

	@Override
	public void setUp() {
		
		loader.populateRepositories();
		halLinkDiscoverer = new HalLinkDiscoverer();
		halFormsLinkDiscoverer = new HalFormsLinkDiscoverer();
		
		super.setUp();
	}

	@Test
	public void tests() throws Exception {

		mvc.perform(get("/")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
			.andDo(print())
			.andExpect(status().isOk());

		mvc.perform(get("/")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk());

		mvc.perform(get("/")
			.header(HttpHeaders.ACCEPT, MediaTypes.COLLECTION_JSON))
			.andDo(print())
			.andExpect(status().isOk());

		String halFormsSingleItemResults = mvc.perform(get("/people/1")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
			.andExpect(jsonPath("$.firstName").value("Billy Bob"))
			.andExpect(jsonPath("$.lastName").value("Thornton"))
			.andExpect(jsonPath("$._links[*]", hasSize(4)))
			.andReturn().getResponse().getContentAsString()
		;

		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_SELF, halFormsSingleItemResults)).isEqualTo(new Link("http://localhost/people/1"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("person", halFormsSingleItemResults)).isEqualTo(new Link("http://localhost/people/1{?projection}", "person"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("siblings", halFormsSingleItemResults)).isEqualTo(new Link("http://localhost/people/1/siblings{?projection}", "siblings"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("father", halFormsSingleItemResults)).isEqualTo(new Link("http://localhost/people/1/father{?projection}", "father"));

		String halFormsSingleItemResults2 = mvc.perform(get("/people/2")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
			.andExpect(jsonPath("$.firstName").value("John"))
			.andExpect(jsonPath("$.lastName").value("Doe"))
			.andExpect(jsonPath("$._links[*]", hasSize(4)))
			.andReturn().getResponse().getContentAsString()
			;

		String halSingleItemResults = mvc.perform(get("/people/1")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._links[*]", hasSize(4)))
			.andReturn().getResponse().getContentAsString();

		assertThat(halLinkDiscoverer.findLinkWithRel(Link.REL_SELF, halSingleItemResults)).isEqualTo(new Link("http://localhost/people/1"));
		assertThat(halLinkDiscoverer.findLinkWithRel("person", halSingleItemResults)).isEqualTo(new Link("http://localhost/people/1{?projection}", "person"));
		assertThat(halLinkDiscoverer.findLinkWithRel("siblings", halSingleItemResults)).isEqualTo(new Link("http://localhost/people/1/siblings{?projection}", "siblings"));
		assertThat(halLinkDiscoverer.findLinkWithRel("father", halSingleItemResults)).isEqualTo(new Link("http://localhost/people/1/father{?projection}", "father"));

		String collectionJsonSingleItemResults = mvc.perform(get("/people/1")
			.header(HttpHeaders.ACCEPT, MediaTypes.COLLECTION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.COLLECTION_JSON_VALUE + ";charset=UTF-8"))
			.andExpect(jsonPath("$.collection.href").value("http://localhost/people/1"))
			.andExpect(jsonPath("$.collection.links[*]", hasSize(3)))
			.andExpect(jsonPath("$.collection.items[0].href").value("http://localhost/people/1"))
			.andExpect(jsonPath("$.collection.items[0].data[0].name").value("firstName"))
			.andExpect(jsonPath("$.collection.items[0].data[0].value").value("Billy Bob"))
			.andExpect(jsonPath("$.collection.items[0].data[1].name").value("lastName"))
			.andExpect(jsonPath("$.collection.items[0].data[1].value").value("Thornton"))
			.andExpect(jsonPath("$.collection.items[0].links[*]", hasSize(3)))
			.andExpect(jsonPath("$.collection.template.data[0].name").value("age"))
			.andExpect(jsonPath("$.collection.template.data[1].name").value("created"))
			.andExpect(jsonPath("$.collection.template.data[2].name").value("father"))
			.andExpect(jsonPath("$.collection.template.data[3].name").value("firstName"))
			.andExpect(jsonPath("$.collection.template.data[4].name").value("gender"))
			.andExpect(jsonPath("$.collection.template.data[5].name").value("height"))
			.andExpect(jsonPath("$.collection.template.data[6].name").value("id"))
			.andExpect(jsonPath("$.collection.template.data[7].name").value("lastName"))
			.andExpect(jsonPath("$.collection.template.data[8].name").value("siblings"))
			.andExpect(jsonPath("$.collection.template.data[9].name").value("weight"))
			.andReturn().getResponse().getContentAsString()
			;

		String collectionJsonSingleItemResults2 = mvc.perform(get("/people/2")
			.header(HttpHeaders.ACCEPT, MediaTypes.COLLECTION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.COLLECTION_JSON_VALUE + ";charset=UTF-8"))
			.andExpect(jsonPath("$.collection.href").value("http://localhost/people/2"))
			.andExpect(jsonPath("$.collection.links[*]", hasSize(3)))
			.andExpect(jsonPath("$.collection.items[0].href").value("http://localhost/people/2"))
			.andExpect(jsonPath("$.collection.items[0].data[0].name").value("firstName"))
			.andExpect(jsonPath("$.collection.items[0].data[0].value").value("John"))
			.andExpect(jsonPath("$.collection.items[0].data[1].name").value("lastName"))
			.andExpect(jsonPath("$.collection.items[0].data[1].value").value("Doe"))
			.andExpect(jsonPath("$.collection.items[0].links[*]", hasSize(3)))
			.andExpect(jsonPath("$.collection.template.data[0].name").value("age"))
			.andExpect(jsonPath("$.collection.template.data[1].name").value("created"))
			.andExpect(jsonPath("$.collection.template.data[2].name").value("father"))
			.andExpect(jsonPath("$.collection.template.data[3].name").value("firstName"))
			.andExpect(jsonPath("$.collection.template.data[4].name").value("gender"))
			.andExpect(jsonPath("$.collection.template.data[5].name").value("height"))
			.andExpect(jsonPath("$.collection.template.data[6].name").value("id"))
			.andExpect(jsonPath("$.collection.template.data[7].name").value("lastName"))
			.andExpect(jsonPath("$.collection.template.data[8].name").value("siblings"))
			.andExpect(jsonPath("$.collection.template.data[9].name").value("weight"))
			.andReturn().getResponse().getContentAsString()
			;

		String halFormsAggregateResults = mvc.perform(get("/people")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._links[*]", hasSize(3)))
			.andExpect(jsonPath("$.page.size").value(20))
			.andExpect(jsonPath("$.page.totalElements").value(3))
			.andExpect(jsonPath("$.page.totalPages").value(1))
			.andExpect(jsonPath("$.page.number").value(0))
			.andReturn().getResponse().getContentAsString();

		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_SELF, halFormsAggregateResults)).isEqualTo(new Link("http://localhost/people{?page,size,sort,projection}"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("profile", halFormsAggregateResults)).isEqualTo(new Link("http://localhost/profile/people", "profile"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("search", halFormsAggregateResults)).isEqualTo(new Link("http://localhost/people/search", "search"));

		String halFormsPageRequest = mvc.perform(get("/people")
			.param("page", "1")
			.param("size", "1")
			.header(HttpHeaders.ACCEPT, MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.people[0].firstName").value("John"))
			.andExpect(jsonPath("$._embedded.people[0].lastName").value("Doe"))
			.andExpect(jsonPath("$._embedded.people[0]._links[*]", hasSize(4)))
			.andExpect(jsonPath("$._links[*]", hasSize(7)))
			.andExpect(jsonPath("$.page.size").value(1))
			.andExpect(jsonPath("$.page.totalElements").value(3))
			.andExpect(jsonPath("$.page.totalPages").value(3))
			.andExpect(jsonPath("$.page.number").value(1))
			.andReturn().getResponse().getContentAsString();

		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_SELF, halFormsPageRequest)).isEqualTo(new Link("http://localhost/people{&sort,projection}"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_FIRST, halFormsPageRequest)).isEqualTo(new Link("http://localhost/people?page=0&size=1", Link.REL_FIRST));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_PREVIOUS, halFormsPageRequest)).isEqualTo(new Link("http://localhost/people?page=0&size=1", Link.REL_PREVIOUS));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_NEXT, halFormsPageRequest)).isEqualTo(new Link("http://localhost/people?page=2&size=1", Link.REL_NEXT));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel(Link.REL_LAST, halFormsPageRequest)).isEqualTo(new Link("http://localhost/people?page=2&size=1", Link.REL_LAST));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("profile", halFormsPageRequest)).isEqualTo(new Link("http://localhost/profile/people", "profile"));
		assertThat(halFormsLinkDiscoverer.findLinkWithRel("search", halFormsPageRequest)).isEqualTo(new Link("http://localhost/people/search", "search"));

		String collectionJsonAggregateResults = mvc.perform(get("/people")
			.header(HttpHeaders.ACCEPT, MediaTypes.COLLECTION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.collection.href").value("http://localhost/people{?page,size,sort,projection}"))
			.andExpect(jsonPath("$.collection.links[*]", hasSize(2)))
			.andExpect(jsonPath("$.collection.items[0].href").value("http://localhost/people/1"))
			.andExpect(jsonPath("$.collection.items[0].data[0].name").value("firstName"))
			.andExpect(jsonPath("$.collection.items[0].data[0].value").value("Billy Bob"))
			.andExpect(jsonPath("$.collection.items[0].data[1].name").value("lastName"))
			.andExpect(jsonPath("$.collection.items[0].data[1].value").value("Thornton"))
			.andExpect(jsonPath("$.collection.items[0].links[*]", hasSize(1)))
			.andExpect(jsonPath("$.collection.template.data[0].name").value("age"))
			.andExpect(jsonPath("$.collection.template.data[1].name").value("created"))
			.andExpect(jsonPath("$.collection.template.data[2].name").value("father"))
			.andExpect(jsonPath("$.collection.template.data[3].name").value("firstName"))
			.andExpect(jsonPath("$.collection.template.data[4].name").value("gender"))
			.andExpect(jsonPath("$.collection.template.data[5].name").value("height"))
			.andExpect(jsonPath("$.collection.template.data[6].name").value("id"))
			.andExpect(jsonPath("$.collection.template.data[7].name").value("lastName"))
			.andExpect(jsonPath("$.collection.template.data[8].name").value("siblings"))
			.andExpect(jsonPath("$.collection.template.data[9].name").value("weight"))
			.andReturn().getResponse().getContentAsString();

		String collectionJsonPageRequest = mvc.perform(get("/people")
			.param("page", "1")
			.param("size", "1")
			.header(HttpHeaders.ACCEPT, MediaTypes.COLLECTION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
//			.andExpect(jsonPath("$._embedded.people[0].firstName").value("John"))
//			.andExpect(jsonPath("$._embedded.people[0].lastName").value("Doe"))
//			.andExpect(jsonPath("$._embedded.people[0]._links[*]", hasSize(4)))
//			.andExpect(jsonPath("$._links[*]", hasSize(7)))
//			.andExpect(jsonPath("$.page.size").value(1))
//			.andExpect(jsonPath("$.page.totalElements").value(3))
//			.andExpect(jsonPath("$.page.totalPages").value(3))
//			.andExpect(jsonPath("$.page.number").value(1))
			.andReturn().getResponse().getContentAsString();
	}
}
