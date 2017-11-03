/**
 * une enumeration des differents etats possibles d'un programme, avec les possibilites
 * Etat Pre etude
 * Etat de financement
 * Execution
 * Achevé
 */
package com.bootcampWS.enums;

/**
 *
 * @author soul
 */
public enum EtatProgramme {
PREETUDE("Etat de pré Etude"),
FINANCEMENT("Etape de financement"),
EXECUTION("Etat d'exécution"),
ACHEVE("Projet achevé");

 private String etatProgramme;

    EtatProgramme(String etatProgramme) {
        this.etatProgramme = etatProgramme;
    }

    public String EtatProgramme() {
        return etatProgramme;
    }


}
