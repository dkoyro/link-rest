package com.nhl.link.rest.runtime.parser.filter;

import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.Entity;

/**
 * @since 1.5
 */
public interface IFilterProcessor {

	void process(DataResponse<?> response, UriInfo uriInfo);

	void process(Entity<?> entity, JsonNode expNode);
}
