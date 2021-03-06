/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.rollup.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilities;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.MasterNodeOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.xpack.core.rollup.job.RollupJobConfig;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class PutRollupJobAction extends Action<PutRollupJobAction.Request, AcknowledgedResponse,
        PutRollupJobAction.RequestBuilder> {

    public static final PutRollupJobAction INSTANCE = new PutRollupJobAction();
    public static final String NAME = "cluster:admin/xpack/rollup/put";

    private PutRollupJobAction() {
        super(NAME);
    }

    @Override
    public RequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new RequestBuilder(client, INSTANCE);
    }

    @Override
    public AcknowledgedResponse newResponse() {
        return new AcknowledgedResponse();
    }

    public static class Request extends AcknowledgedRequest<Request> implements IndicesRequest, ToXContentObject {

        private RollupJobConfig config;
        private IndicesOptions indicesOptions = IndicesOptions.fromOptions(false, false, true, false);

        public Request(RollupJobConfig config) {
            this.config = config;
        }

        public Request() {

        }

        public static Request fromXContent(final XContentParser parser, final String id) throws IOException {
            return new Request(RollupJobConfig.fromXContent(parser, id));
        }

        public RollupJobConfig getConfig() {
            return config;
        }

        public void setConfig(RollupJobConfig config) {
            this.config = config;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            this.config = new RollupJobConfig(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            this.config.writeTo(out);
        }

        @Override
        public ActionRequestValidationException validate() {
            return null;
        }

        public RollupActionRequestValidationException validateMappings(Map<String, Map<String, FieldCapabilities>> fieldCapsResponse) {
            RollupActionRequestValidationException validationException = new RollupActionRequestValidationException();
            if (fieldCapsResponse.size() == 0) {
                validationException.addValidationError("Could not find any fields in the index/index-pattern that were configured in job");
                return validationException;
            }
            config.validateMappings(fieldCapsResponse, validationException);
            if (validationException.validationErrors().size() > 0) {
                return validationException;
            }
            return null;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            return this.config.toXContent(builder, params);
        }

        @Override
        public String[] indices() {
            return new String[]{this.config.getIndexPattern()};
        }

        @Override
        public IndicesOptions indicesOptions() {
            return indicesOptions;
        }

        @Override
        public int hashCode() {
            return Objects.hash(config);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Request other = (Request) obj;
            return Objects.equals(config, other.config);
        }
    }

    public static class RequestBuilder extends MasterNodeOperationRequestBuilder<Request, AcknowledgedResponse, RequestBuilder> {

        protected RequestBuilder(ElasticsearchClient client, PutRollupJobAction action) {
            super(client, action, new Request());
        }
    }
}
