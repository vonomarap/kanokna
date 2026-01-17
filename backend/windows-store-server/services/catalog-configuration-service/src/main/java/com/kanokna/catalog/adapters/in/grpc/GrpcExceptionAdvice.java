package com.kanokna.catalog.adapters.in.grpc;

import com.kanokna.catalog.domain.exception.InvalidConfigurationException;
import com.kanokna.catalog.domain.exception.ProductTemplateNotFoundException;
import com.kanokna.catalog.domain.exception.RuleSetNotFoundException;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@Component
@GlobalServerInterceptor
@Order(1)
public class GrpcExceptionAdvice implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (ProductTemplateNotFoundException ex) {
                    call.close(Status.NOT_FOUND.withDescription(ex.getMessage()), new Metadata());
                } catch (RuleSetNotFoundException ex) {
                    call.close(Status.FAILED_PRECONDITION.withDescription(ex.getMessage()), new Metadata());
                } catch (InvalidConfigurationException ex) {
                    call.close(Status.FAILED_PRECONDITION.withDescription(ex.getMessage()), new Metadata());
                } catch (IllegalArgumentException ex) {
                    call.close(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()), new Metadata());
                } catch (Exception ex) {
                    call.close(Status.INTERNAL.withDescription("Catalog configuration service error")
                        .withCause(ex), new Metadata());
                }
            }
        };
    }
}
