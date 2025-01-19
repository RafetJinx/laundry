package com.laundry.helper;

import com.laundry.exception.AccessDeniedException;

public class RoleGuard {
    /**
     * Ensures that the user has the ROLE_ADMIN role.
     * If not, an {@link AccessDeniedException} is thrown with the provided message.
     *
     * @param currentUserRole the role of the current user
     * @param errorMessage    the error message to include in the exception if not ADMIN
     * @throws AccessDeniedException if the user is not an admin
     */
    public static void requireAdminRole(String currentUserRole, String errorMessage) {
        if (!"ROLE_ADMIN".equals(currentUserRole)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    /**
     * Ensures that the user is either an admin or the owner of the resource.
     * Ownership is determined by comparing the entityOwnerId to the currentUserId.
     * If neither condition is true, an {@link AccessDeniedException} is thrown.
     *
     * @param currentUserRole the role of the current user
     * @param currentUserId   the ID of the current user
     * @param entityOwnerId   the owner ID of the resource
     * @param errorMessage    the error message to include in the exception if neither admin nor owner
     * @throws AccessDeniedException if the user is neither admin nor owner
     */
    public static void requireAdminOrOwner(String currentUserRole,
                                           Long currentUserId,
                                           Long entityOwnerId,
                                           String errorMessage) {
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole);
        boolean isOwner = entityOwnerId != null && entityOwnerId.equals(currentUserId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
