package com.kanokna.account.application.port.out;

/**
 * Outbound port for accessing current user identity information.
 */
public interface CurrentUserProvider {
    CurrentUser currentUser();
}