package niwer.photon.util;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectPurchase;
import niwer.photon.sql.PurchaseTable;
import niwer.photon.web.api.github.AddTeamMemberRequest;
import niwer.photon.web.api.github.CreateRepositoryRequest;
import niwer.photon.web.api.github.RemoveRepositoryCollaboratorRequest;
import niwer.photon.web.api.github.RemoveTeamMemberRequest;
import niwer.photon.web.api.github.SetRepositoryPermissionsRequest;

/**
 * This class handles the provisioning and revocation of GitHub access for users based on their Stripe subscription status.
 * 
 * @author Niwer
 */
public final class GitHubProvisioningService {

    private GitHubProvisioningService() {}

    /**
     * Provisions GitHub access for a user asynchronously. This method creates a repository, sets permissions, and adds the user to the team.
     * 
     * @param githubUsername The GitHub username of the user to provision access for.
     */
    public static void provisionAsync(String githubUsername) {
        if (githubUsername == null || githubUsername.isBlank()) return;

        Thread.ofVirtual().start(() -> {
            try {
                new CreateRepositoryRequest(githubUsername).request();
                Thread.sleep(2500);
                new SetRepositoryPermissionsRequest(githubUsername, "admin").request();
                new AddTeamMemberRequest(githubUsername).request();
            } catch (Exception e) {
                Console.log("Failed GitHub provisioning for " + githubUsername + ": " + e.getMessage())
                    .type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }

    /**
     * Revokes GitHub access for a user asynchronously. This method removes the user from the repository and team.
     * 
     * @param stripeCustomerId The Stripe customer ID associated with the user whose GitHub access is to be revoked.
     */
    public static void revokeAsync(String stripeCustomerId) {
        if (stripeCustomerId == null || stripeCustomerId.isBlank()) return;

        Thread.ofVirtual().start(() -> {
            try {
                final ObjectPurchase purchase = PurchaseTable.getByCustomerId(stripeCustomerId);
                if (purchase != null && purchase.githubUsername() != null && !purchase.githubUsername().isBlank()) {
                    new RemoveRepositoryCollaboratorRequest(purchase.githubUsername()).request();
                    new RemoveTeamMemberRequest(purchase.githubUsername()).request();
                }
            } catch (Exception e) {
                Console.log("Error revoking GitHub access for customer " + stripeCustomerId + ": " + e.getMessage())
                    .type(PhotonLogTypes.STRIPE).error().container(PhotonEngine.LOGGER).send();
            }
        });
    }
}