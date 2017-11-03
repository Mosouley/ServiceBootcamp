/**
 * implementation du repository sur Programme
 * classe heritant du baserepository
 * gardons l'unite de persitance en variable pour pouvoir la modifier
 * a tout moment
 */
package com.bootcampWS.jpa;

import com.bootcampWS.entities.Programme;

/**
 *
 * @author soul
 */
public class ProgrammeRepository extends BaseRepository<Programme> {
public ProgrammeRepository(String persistUnit) {
        super(persistUnit,Programme.class);
    }
}
