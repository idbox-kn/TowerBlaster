package controllers;

import models.Activegame;
import models.Business;
import play.mvc.*;
import play.db.jpa.*;
import scala.util.parsing.combinator.testing.Str;
import views.html.*;
import models.Person;
import play.data.Form;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Query;

import static play.libs.Json.*;

public class Application extends Controller {
    private SecureRandom random = new SecureRandom();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date date;
    private Security security = new Security();

    @Transactional
    public Result index() {
        return ok(index.render(""));
    }

    @Transactional
    public Result loginSuccess() {
        return ok(clientlogin.render("","",""));
    }

    @Transactional
    public Result joinGame() {
        Business a1 = Form.form(Business.class).bindFromRequest().get();
        System.out.println("From form: " + a1.respA + "test" + a1.respC);
        Person p = Person.find.query().eq("nickname", a1.respC).findUnique();
        return ok(activegame.render(p.nickname,p.currentScore,p.highScore,p.sessionId));
    }

    @Transactional
    public Result joinedGame() {
        List<Activegame> a2 = Activegame.find.all();
        Business a1 = Form.form(Business.class).bindFromRequest().get();
        System.out.println("Fra form " + a1.respC);

        for(Activegame a : a2){
            System.out.println("Fra tabell " + a.sessionId);
            System.out.println("Fra form2 " +a1.respC);
            if(a1.respC.equals(a.sessionId)){
                return ok(toJson(a));
            }
        }
        return ok();
    }

    @Transactional
    public String se() {
        List<Activegame> a2 = Activegame.find.all();
        Business a1 = Form.form(Business.class).bindFromRequest().get();
        System.out.println("Fra form " + a1.respC);

        for(Activegame a : a2){
            System.out.println("Fra tabell " +a.sessionId);
            System.out.println("Fra form2 " +a1.respC);
            if(a1.respC.equals(a.sessionId)){
                return "" + toJson(a);
            }
        }
        return "";
    }


    @Transactional
    public Result webLogin() {
        Person person = Form.form(Person.class).bindFromRequest().get();
        Person authenticatedPerson = security.webLogin(person);
/*
        ArrayList<Integer> test = new ArrayList<Integer>();
        test.add(1);
        test.add(2);
        test.add(3);


        Activegame ag = new Activegame();
        ag.gamelist = test;
        ag.p1list = test;
        ag.p2list = test;
        ag.player1 = "Kake";
        ag.player2 = "Millus";
        ag.sessionId = "123";
        ag.save();
*/
        if(authenticatedPerson != null) {
            /*
            List<Activegame> a2 = Activegame.find.all();

            for(Activegame a : a2){
                System.out.println(a.gamelist.get(1));
            }
            */

            return ok(clientlogin.render(authenticatedPerson.nickname, authenticatedPerson.currentScore,authenticatedPerson.highScore));

        }
        return ok(index.render("Wrong username/password"));
    }

    //UNDER DEVELOPMENT
    @Transactional(readOnly = true)
    public Result getActiveGamesFromUser() {
        Business b = Form.form(Business.class).bindFromRequest().get();
        System.out.println("Fikk info: "+ b.respC);
        Person person=Person.find.query().eq("nickname",b.respC).findUnique();
        if(person!=null) {
            List<Person> players = Person.find.query().eq("sessionId",person.sessionId).findList();
            Person player2 = players.get(0).nickname.equals(person.nickname)?players.get(1):players.get(0);
            return ok(toJson(player2));
        }
        return ok();
    }







    //Function for generating a random sessionkey
    private String generateSessionKey(){
        return new BigInteger(130, random).toString(32);
    }
    //Function for generating a random NFC key
    private String generateNFCKey(){
        return new BigInteger(40, random).toString(32);
    }



/*
    @Transactional
    //Logging a user out from the system.
    public Result logout() {
        date = new Date();
        String logdate = (dateFormat.format(date));
        //Getting the IP-address from the user
        String remoteAddress = request().remoteAddress();
        Person person = Form.form(Person.class).bindFromRequest().get();
        Person authenticatedPerson = security.authenticate(person);

        if(authenticatedPerson != null){
            //Open access to the log and storing the action from the user in the log.
            try(FileWriter fw = new FileWriter("Logsfromclients/" + authenticatedPerson.firstname + "-" +authenticatedPerson.phoneNumber + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                //Printing to log
                out.println("LOGOUT.");
                out.println("Date: " + logdate);
                out.println(authenticatedPerson.firstname + " " + authenticatedPerson.lastname);
                out.println("Phone: " + authenticatedPerson.phoneNumber);
                out.println("Is about to log out");
                out.println("Access done from: " + remoteAddress);
                out.println();
                //Actuall saving the data to profile in DB
                authenticatedPerson.sessionId="";
                authenticatedPerson.save();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            //If user is not authorized, then we will receieve a error message and log the IP-address from the user for further investigating.
            try(FileWriter fw = new FileWriter("Logsfromclients/unauthorizedErrors.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println("ERROR");
                out.println("Unauthorized user tried to LOGOUT");
                out.println("Access done from: " + remoteAddress);
                out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ok();
    }

    @Transactional
    public Result login() {
        date = new Date();
        String logdate = (dateFormat.format(date));
        //Getting the IP-address from the user
        String remoteAddress = request().remoteAddress();
        Person person = Form.form(Person.class).bindFromRequest().get();
        Person authenticatedPerson = security.login(person);

        if(authenticatedPerson != null){
            //Generating a new session-id if user is not logged in anywhere else.
            if(authenticatedPerson.sessionId.equals("")){
                authenticatedPerson.sessionId=generateSessionKey();
            }

            //Open access to the log and storing the action from the user in the log.
            try(FileWriter fw = new FileWriter("Logsfromclients/" + authenticatedPerson.firstname + "-" +authenticatedPerson.phoneNumber + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {


                //Printing to log
                out.println("LOGIN.");
                out.println("Date: " + logdate);
                out.println("Successfull login from: ");
                out.println(authenticatedPerson.firstname+ " " + authenticatedPerson.lastname );
                out.println("Phone: " + authenticatedPerson.phoneNumber);
                out.println("Access done from: " + remoteAddress);
                out.println();

                //Save the data to DB
                authenticatedPerson.save();
                //Return the user-data from DB to user
                return ok(toJson(authenticatedPerson));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //If user is not authorized, then we will receieve a error message and log the IP-address from the user for further investigating.
            try(FileWriter fw = new FileWriter("Logsfromclients/unauthorizedErrors.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println("ERROR");
                out.println("Unauthorized user tried to LOGIN");
                out.println("Access done from: " + remoteAddress);
                out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ok();
    }


*/







}
