/**
 * une enumeration des differents etats possibles d'un programme, avec les possibilites
 * Etat Pre etude
 * Etat de financement
 * Execution
 * Achev�
 */
package com.bootcampWS.enums;

/**
 *
 * @author soul
 */
public enum EtatProgramme {
PREETUDE("Etat de pr� Etude"),
FINANCEMENT("Etape de financement"),
EXECUTION("Etat d'ex�cution"),
ACHEVE("Projet achev�");

 private String etatProgramme;

    EtatProgramme(String etatProgramme) {
        this.etatProgramme = etatProgramme;
    }

    public String EtatProgramme() {
        return etatProgramme;
    }


}
