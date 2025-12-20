package com.photon.discord;

/**
 * enume to definde roles
 * 
 * @author Mini
 */
public enum Roles {

    // Hight staff
    FOUNDER(1323747260667789344L),
    ADMIN(1323747265583775877L),
    HIGHSTAFF(1323747262995628075L),

    // Staff
    DEVELOPER(1324719792397094952L),
    STAFF(1323747270193316051L),

    // Member
    MUTE(1324033956118663310L),
    FR(1323747272244203562L),
    EN(1323747272244203562L);

    public long id;

    /**
     * create a role
     * 
     * @param id the id of the role
     */
    private Roles(long id) {
        this.id = id;
    }

}
