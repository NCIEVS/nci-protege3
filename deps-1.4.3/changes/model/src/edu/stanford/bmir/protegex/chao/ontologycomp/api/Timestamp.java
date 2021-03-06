package edu.stanford.bmir.protegex.chao.ontologycomp.api;

import java.io.Serializable;
import java.util.Date;

/**
 * Generated by Protege (http://protege.stanford.edu).
 * Source Class: Timestamp
 *
 * @version generated on Mon Aug 18 21:08:59 GMT-08:00 2008
 */
public interface Timestamp extends Serializable {

    // Slot date

    String getDate();

    boolean hasDate();

    void setDate(String newDate);


    // Slot sequence

    int getSequence();

    boolean hasSequence();

    void setSequence(int newSequence);

    void delete();

    //__Following code is not automatically generated
    int compareTimestamp(Timestamp timestamp);

    Date getDateParsed();
}
