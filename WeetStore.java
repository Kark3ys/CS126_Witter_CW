/**
 * Your preamble here
 *
 * @author: Your university ID
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class WeetStore implements IWeetStore {

    public WeetStore() {
    }

    public boolean addWeet(Weet weet) {
        // TODO 
        return false;
    }
    
    public Weet getWeet(int wid) {
        // TODO
        return null;
    }

    public Weet[] getWeets() {
        // TODO 
        return null;
    }

    public Weet[] getWeetsByUser(User usr) {
        // TODO 
        return null;
    }

    public Weet[] getWeetsContaining(String query) {
        // TODO
        return null;
    }

    public Weet[] getWeetsOn(Date dateOn) {
        // TODO 
        return null;
    }

    public Weet[] getWeetsBefore(Date dateBefore) {
        // TODO 
        return null;
    }

    public String[] getTrending() {
        // TODO
        return null;
    }

}
