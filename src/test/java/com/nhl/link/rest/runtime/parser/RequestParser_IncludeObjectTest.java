package com.nhl.link.rest.runtime.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.DataMap;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.filter.FilterProcessor;
import com.nhl.link.rest.runtime.parser.filter.IFilterProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.update.UpdateFilter;

public class RequestParser_IncludeObjectTest extends TestWithCayenneMapping {

	private RequestParser parser;

	@Before
	public void setUp() {

		ObjectContext sharedContext = runtime.newContext();
		ICayennePersister cayenneService = mock(ICayennePersister.class);
		when(cayenneService.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
		when(cayenneService.sharedContext()).thenReturn(sharedContext);
		when(cayenneService.newContext()).thenReturn(runtime.newContext());
		IMetadataService metadataService = new MetadataService(Collections.<DataMap> emptyList(), cayenneService);

		IPathCache pathCache = new PathCache();
		IJacksonService jacksonService = new JacksonService();
		ISortProcessor sortProcessor = new SortProcessor(jacksonService, pathCache);
		IFilterProcessor filterProcessor = new FilterProcessor(jacksonService, pathCache);
		ITreeProcessor treeProcessor = new IncludeExcludeProcessor(jacksonService, sortProcessor, filterProcessor);

		parser = new RequestParser(Collections.<UpdateFilter> emptyList(), metadataService, jacksonService,
				new RelationshipMapper(), treeProcessor, sortProcessor, filterProcessor);
	}

	@Test
	public void testToDataRequest_IncludeObject_Path() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\"}"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);
		assertTrue(clientEntity.isIdIncluded());

		assertEquals(1, clientEntity.getChildren().size());
		assertTrue(clientEntity.getChildren().containsKey(E2.E3S.getName()));
	}

	@Test
	public void testToDataRequest_IncludeObject_MapBy() {

		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
		when(params.get("include")).thenReturn(Arrays.asList("{\"path\":\"e3s\",\"mapBy\":\"e5\"}"));

		UriInfo urlInfo = mock(UriInfo.class);
		when(urlInfo.getQueryParameters()).thenReturn(params);

		DataResponse<E2> dataRequest = DataResponse.forType(E2.class);
		parser.parseSelect(dataRequest, urlInfo, null);

		assertNotNull(dataRequest);
		Entity<E2> clientEntity = dataRequest.getEntity();
		assertNotNull(clientEntity);

		Entity<?> mapBy = clientEntity.getChildren().get(E2.E3S.getName()).getMapBy();
		assertNotNull(mapBy);
		assertNotNull(mapBy.getChildren().get(E3.E5.getName()));
	}
}
