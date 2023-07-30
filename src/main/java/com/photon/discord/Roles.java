package com.photon.discord;
/**
 * enume to definde roles
 * @author Mini
 */
public enum Roles {
    

    // Hight staff
    FOUNDER(1091681102495760455L),
    ADMIN(1108485032135364608L),
    HIGHSTAFF(1107039473180622931L),

    // Staff
    DEVELOPER(1091683885143826542L),
    STAFF(1091683847206359040L),
    

    // Member
    MUTE(1134122689326485504L),
    FR(1091681759076294736L),
    EN(1091681719180079104L);
    

    public long id;

    /**
     * create a role
     * @param id the id of the role
     */
    private Roles(long id) {
        this.id = id;
    }

}
