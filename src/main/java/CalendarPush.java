/**
 * Created by rishabh on 14/07/15.
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CalendarPush {

    private static final Logger log = Logger.getLogger(CalendarPush.class.getName());
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";
    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-api-quickstart");
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the scopes required by this quickstart.
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR);
    /**
     * Properties Load
     */
    static Properties prop = new Properties();
    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private int DATE = 14;
    private int MONTH = 06;
    private int YEAR = 2015;
    private int hour = 16;
    private int min = 00;

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                CalendarPush.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {

        CalendarPush calObj = new CalendarPush();


        Date curDate = new Date(calObj.YEAR - 1900, calObj.MONTH,
                calObj.DATE, calObj.hour, calObj.min);

        log.log(Level.INFO,"Starting the word cycle from time {0}",curDate);
        // System.out.println(curDate);
        InputStream input = CalendarPush.class.getResourceAsStream("/dictionary.properties");

        prop.load(input);

        Set<Object> propKeys = prop.keySet();

        int counter = 0;
        for (Object k : propKeys) {

            //Date Calculation
            Date tempStart = new Date();
            tempStart.setHours(curDate.getHours() + counter);
            Date tempEnd = new Date();
            tempEnd.setHours(tempStart.getHours() + 1);
            counter++;
            String keyName = (String) k;
            String valName = prop.getProperty(keyName);

            String displayString = keyName + " -> " + valName;
            String description = "Words Learning 1.0";

            log.log(Level.INFO, "Creating Event for word {0} at time {1} ", new Object[]{keyName, tempStart});
            calObj.createCalEvent(tempStart, tempEnd, displayString, description);
        }
        System.out.println("DONE!");


    }

    public void createCalEvent(Date startDate, Date endDate, String summary, String desc) throws IOException {

        com.google.api.services.calendar.Calendar service =
                getCalendarService();
        Event createEvent = new Event()
                .setSummary(summary)
                .setDescription(desc);

        DateTime startTime = new DateTime(startDate);
        DateTime endTime = new DateTime(endDate);

        EventDateTime start = new EventDateTime()
                .setDateTime(startTime)
                .setTimeZone("Asia/Kolkata");

        createEvent.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(endTime)
                .setTimeZone("Asia/Kolkata");

        createEvent.setEnd(end);

        EventAttendee[] attendees = new EventAttendee[]{
                new EventAttendee().setEmail("rbnext29@gmail.com"),
                new EventAttendee().setEmail("nehaj1993@gmail.com"),
                new EventAttendee().setEmail("explorepulkit@gmail.com")
        };

        createEvent.setAttendees(Arrays.asList(attendees));

        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("popup").setMinutes(10)
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));

        createEvent.setReminders(reminders);

        String calendarId = "primary";
        createEvent = service.events().insert(calendarId, createEvent).execute();
        System.out.printf("Event created: %s\n", createEvent.getHtmlLink());
    }

}
