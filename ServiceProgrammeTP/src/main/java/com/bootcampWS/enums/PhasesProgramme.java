
package com.bootcampWS.enums;

/**
 *Enumeration des phases d'un programme
 * @author soul
 */
public enum PhasesProgramme {
    PHASE1("Phase 1"),
    PHASE2("Phase 2"),
    PHASE3("Phase 3");

    private String phasesProgramme;

    private PhasesProgramme(String phasesProgramme) {
        this.phasesProgramme = phasesProgramme;
    }
     public String PhasesProgramme() {
        return phasesProgramme;
    }
}
