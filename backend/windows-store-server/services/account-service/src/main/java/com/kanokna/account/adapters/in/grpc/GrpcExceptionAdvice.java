package com.kanokna.account.adapters.in.grpc;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import com.kanokna.shared.core.DomainException;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * gRPC exception interceptor for account service.
 * Converts domain and application exceptions to appropriate gRPC Status codes.
 * 
 * Spring gRPC uses ServerInterceptor for exception handling instead of @GrpcAdvice.
 */
@Component
@GlobalServerInterceptor
@Order(1)
public class GrpcExceptionAdvice implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (DomainException ex) {
                    Status status = mapDomainException(ex);
                    call.close(status, new Metadata());
                } catch (IllegalArgumentException ex) {
                    call.close(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()), new Metadata());
                } catch (Exception ex) {
                    call.close(Status.INTERNAL.withDescription("Account service error").withCause(ex), new Metadata());
                }
            }
        };
    }

    private Status mapDomainException(DomainException ex) {
        return switch (ex.getCode()) {
            case "ERR-ACCT-UNAUTHORIZED" -> Status.PERMISSION_DENIED.withDescription(ex.getMessage());
            case "ERR-ACCT-PROFILE-NOT-FOUND",
                 "ERR-ACCT-ADDRESS-NOT-FOUND" -> Status.NOT_FOUND.withDescription(ex.getMessage());
            case "ERR-ACCT-ADDRESS-DUPLICATE" -> Status.ALREADY_EXISTS.withDescription(ex.getMessage());
            case "ERR-ACCT-INVALID-PHONE",
                 "ERR-ACCT-INVALID-LANGUAGE",
                 "ERR-ACCT-INVALID-CURRENCY",
                 "ERR-ACCT-INVALID-CONFIG-NAME",
                 "ERR-ACCT-INVALID-CONFIG-SNAPSHOT",
                 "ERR-ACCT-INVALID-ADDRESS",
                 "ERR-ACCT-LABEL-TOO-LONG" -> Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
            case "ERR-ACCT-CONCURRENT-MODIFICATION" -> Status.ABORTED.withDescription(ex.getMessage());
            case "ERR-ACCT-KEYCLOAK-UNAVAILABLE" -> Status.UNAVAILABLE.withDescription(ex.getMessage());
            default -> Status.INTERNAL.withDescription(ex.getMessage());
        };
    }
}

