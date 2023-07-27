package com.photon.discord;

public enum Roles {
    /* Class to definde roles */

    // Hight staff
    FOUNDER(1091681102495760455L),
    ADMIN(1108485032135364608L),
    HIGHSTAFF(1107039473180622931L),

    // Staff
    DEVELOPER(1091683885143826542L),
    STAFF(1091683847206359040L),
    

    // Member
    MUTE(1134122689326485504L);

    public long id;

    /**
     * create a role
     * @param id the id of the role
     */
    private Roles(long id) {
        this.id = id;
    }

}
