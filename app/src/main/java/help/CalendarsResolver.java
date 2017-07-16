package help;


import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


/**
 * 日程管理提供者provider
 */
public class CalendarsResolver {

    private ContentResolver resolver;

    /**
     * 使用以下Uri时，Android版本>=14; 注意引用包路径：android.provider.CalendarContract下的；
     **/
    private Uri calendarsUri = Calendars.CONTENT_URI;
    private Uri eventsUri = Events.CONTENT_URI;
    private Uri remindersUri = Reminders.CONTENT_URI;
    private Uri attendeesUri = Attendees.CONTENT_URI;

    /**
     * Calendars table columns
     */
    public static final String[] CALENDARS_COLUMNS = new String[]{
            Calendars._ID, // 0
            Calendars.ACCOUNT_NAME, // 1
            Calendars.CALENDAR_DISPLAY_NAME, // 2
            Calendars.OWNER_ACCOUNT // 3
    };

    /**
     * Events table columns
     */
    public static final String[] EVENTS_COLUMNS = new String[]{Events._ID,
            Events.CALENDAR_ID, Events.TITLE, Events.DESCRIPTION,
            Events.EVENT_LOCATION, Events.DTSTART, Events.DTEND,
            Events.EVENT_TIMEZONE, Events.HAS_ALARM, Events.ALL_DAY,
            Events.AVAILABILITY, Events.ACCESS_LEVEL, Events.STATUS,};
    /**
     * Reminders table columns
     */
    public static final String[] REMINDERS_COLUMNS = new String[]{
            Reminders._ID, Reminders.EVENT_ID, Reminders.MINUTES,
            Reminders.METHOD,};
    /**
     * Reminders table columns
     */
    public static final String[] ATTENDEES_COLUMNS = new String[]{
            Attendees._ID, Attendees.ATTENDEE_NAME, Attendees.ATTENDEE_EMAIL,
            Attendees.ATTENDEE_STATUS};

    /**
     * 构造方法
     *
     * @param resolver
     */
    public CalendarsResolver(ContentResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * 获取时区
     *
     * @return String[]
     */
    public static String[] getTimeZones() {
        return TimeZone.getAvailableIDs();
    }

    /**
     * 更新日程提醒
     *
     * @param param
     * @return Uri
     * @throws Exception
     */
    public Map<String, String> updateReminder(Map<String, String> param) {
        Map<String, String> result = new HashMap<String, String>();
        if (Utils.isEmpty(param)) {
            result.put("result", "0");
            result.put("obj", "日程提醒更新参数为空！");
            return result;
        }
        String id = param.get("id");
        if (!isExistReminder(Long.parseLong(id))) {
            result.put("result", "0");
            result.put("obj", "日程提醒id无效，id不存在！");
            return result;
        }
        String mimutes = param.get("mimutes");// 提醒在事件前几分钟后发出
        String method = param.get("method");// 提醒方法:METHOD_DEFAULT:0,*_ALERT:1,*_EMAIL:2,*_SMS:3

        if (Utils.isEmpty(mimutes) && Utils.isEmpty(method)) {
            result.put("result", "0");
            result.put("obj", "日程提醒更新的信息不能都为空！");
            return result;
        }
        ContentValues reminderVal = new ContentValues();

        if (!Utils.isEmpty(mimutes)) {
            // 提醒时间
            int m = Utils.isNumber(mimutes) ? Integer.parseInt(mimutes) : 0;
            reminderVal.put(Reminders.MINUTES, m);// 提醒在事件前多少分钟后发出
        }
        if (!Utils.isEmpty(method)) {
            // 提醒方法
            int methodType = Reminders.METHOD_DEFAULT;
            if (method.equals("1")) {
                methodType = Reminders.METHOD_ALERT;
            } else if (method.equals("2")) {
                methodType = Reminders.METHOD_EMAIL;
            } else if (method.equals("3")) {
                methodType = Reminders.METHOD_SMS;
            }
            reminderVal.put(Reminders.METHOD, methodType);
        }

        try {
            int n = resolver.update(remindersUri, reminderVal, Reminders._ID
                    + "=" + id, null);
            result.put("result", "1");
            result.put("obj", n + "");
        } catch (Exception e) {
            Log.i(Const.APPTAG, e.getMessage());
            result.put("result", "-1");
            result.put("obj", e.getMessage());
        }
        return result;
    }

    /**
     * 更新日程参与者
     *
     * @param param
     * @return Uri
     * @throws Exception
     */
    public Map<String, String> updateAttendee(Map<String, String> param) {
        Map<String, String> result = new HashMap<String, String>();
        if (Utils.isEmpty(param)) {
            result.put("result", "0");
            result.put("obj", "更新参数为空！");
            return null;
        }
        String id = param.get("id");
        if (!isExistAttendee(Long.parseLong(id))) {
            result.put("result", "0");
            result.put("obj", "参与者id无效，id不存在！");
            return null;
        }
        String name = param.get("name");// 参与者姓名
        String email = param.get("email");// 参与者电子邮件
        if (Utils.isEmpty(name) && Utils.isEmpty(email)) {
            result.put("result", "0");
            result.put("obj", "参与人更新的信息不能都为空！");
            return result;
        }
        ContentValues attendeesVal = new ContentValues();
        if (!Utils.isEmpty(email)) {
            attendeesVal.put(Attendees.ATTENDEE_NAME, name);
        }
        if (!Utils.isEmpty(email)) {
            attendeesVal.put(Attendees.ATTENDEE_EMAIL, email);// 参与者 email
        }

        try {
            int n = resolver.update(attendeesUri, attendeesVal, Attendees._ID
                    + "=" + id, null);
            result.put("result", "1");
            result.put("obj", n + "");
        } catch (Exception e) {
            Log.i(Const.APPTAG, e.getMessage());
            result.put("result", "-1");
            result.put("obj", e.getMessage());
        }
        return null;
    }

    /**
     * 更新日程事件
     *
     * @param param
     * @return
     */
    public Map<String, String> updateEvent(Map<String, String> param) {
        Map<String, String> result = new HashMap<String, String>();
        if (Utils.isEmpty(param)) {
            result.put("result", "0");
            result.put("obj", "更新参数为空！");
            return null;
        }
        String id = param.get("id");
        if (!isExistEvent(Long.parseLong(id))) {
            result.put("result", "0");
            result.put("obj", "事件id不能为空！");
            return result;
        }

        String calendarId = param.get("calendarId");
        String title = param.get("title");
        String description = param.get("description");
        String location = param.get("location");
        String startDate = param.get("startDate");
        String endDate = param.get("endDate");
        String status = param.get("status");
        String timeZone = param.get("timeZone");
        String hasAlarm = param.get("hasAlarm");
        String allDay = param.get("allDay");
        String availability = param.get("availability");
        String accessLevel = param.get("accessLevel");

        if (!Utils.isNumber(calendarId) && Utils.isEmpty(title)
                && Utils.isEmpty(description) && Utils.isEmpty(location)
                && Utils.isEmpty(startDate) && Utils.isEmpty(endDate)
                && Utils.isEmpty(status)) {
            result.put("result", "0");
            result.put("obj", "事件更新的信息不能都为空！");
            return result;
        }
        ContentValues values = new ContentValues();

        if (Utils.isNumber(calendarId)) {
            values.put(Events.CALENDAR_ID, calendarId);
        }
        if (!Utils.isEmpty(title)) {
            values.put(Events.TITLE, title);
        }
        if (!Utils.isEmpty(description)) {
            values.put(Events.DESCRIPTION, description);
        }
        if (!Utils.isEmpty(location)) {
            values.put(Events.EVENT_LOCATION, location);
        }

        // 计算开始、结束时间，全部用Date也可以。
        Calendar startCld = Calendar.getInstance();
        Calendar endCld = Calendar.getInstance();
        // 如果是全天事件的话，取开始时间的那一整天
        if ((Utils.isNumber(allDay) && Integer.parseInt(allDay) == 1)
                && !Utils.isEmpty(startDate)) {
            Calendar cld = Calendar.getInstance();
            cld = Utils.parseStrToCld(startDate);
            // 开始时间
            startCld.set(cld.get(Calendar.YEAR), cld.get(Calendar.MONTH),
                    cld.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            // 结束时间
            endCld.set(cld.get(Calendar.YEAR), cld.get(Calendar.MONTH),
                    cld.get(Calendar.DAY_OF_MONTH), 24, 0, 0);
            values.put(Events.ALL_DAY, true);
            values.put(Events.DTSTART, startCld.getTimeInMillis());
            values.put(Events.DTEND, endCld.getTimeInMillis());
        } else {
            // 开始时间
            startCld = Utils.parseStrToCld(startDate);
            // 结束时间
            endCld = Utils.parseStrToCld(endDate);
            if (!Utils.isEmpty(startDate)) {
                values.put(Events.DTSTART, startCld.getTimeInMillis());
            }
            if (!Utils.isEmpty(endDate)) {
                values.put(Events.DTEND, endCld.getTimeInMillis());
            }
        }
        if (!Utils.isEmpty(timeZone)) {
            values.put(Events.EVENT_TIMEZONE, timeZone);
        }
        if (Utils.isNumber(hasAlarm)) {
            values.put(Events.HAS_ALARM, hasAlarm);
        }
        if (Utils.isNumber(availability)) {
            values.put(Events.AVAILABILITY, availability);
        }
        if (Utils.isNumber(accessLevel)) {
            values.put(Events.ACCESS_LEVEL, accessLevel);
        }
        try {
            int n = resolver.update(eventsUri, values, Events._ID + "=" + id,
                    null);
            result.put("result", "1");
            result.put("obj", n + "");
        } catch (Exception e) {
            Log.i(Const.APPTAG, e.getMessage());
            result.put("result", "-1");
            result.put("obj", e.getMessage());
        }
        return result;
    }

    /**
     * 查询日程(事件、提醒、参与人)
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> queryEvents(Map<String, String> param) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        StringBuffer selection = new StringBuffer();
        List<String> selectionArgs = new ArrayList<String>();
        if (!Utils.isEmpty(param)) {
            selection.append(" 1=1 ");

            String calendarId = param.get("calendarId");
            String eventId = param.get("id");
            String title = param.get("title");
            String description = param.get("description");
            String location = param.get("location");
            String startDate = param.get("startDate");
            String endDate = param.get("endDate");
            String status = param.get("status");

            if (Utils.isNumber(calendarId)) {
                selection.append(" AND " + Events.CALENDAR_ID + "=? ");
                selectionArgs.add(calendarId);
            }
            if (Utils.isNumber(eventId)) {
                selection.append(" AND " + Events._ID + "=? ");
                selectionArgs.add(eventId);
            }
            if (!Utils.isEmpty(title)) {
                selection.append(" AND " + Events.TITLE + " LIKE ? ");
                selectionArgs.add("%" + title + "%");
            }
            if (!Utils.isEmpty(description)) {
                selection.append(" AND " + Events.DESCRIPTION + " LIKE ? ");
                selectionArgs.add("%" + description + "%");
            }
            if (!Utils.isEmpty(location)) {
                selection.append(" AND " + Events.EVENT_LOCATION + " LIKE ? ");
                selectionArgs.add("%" + location + "%");
            }
            if (Utils.isNumber(status)) {
                selection.append(" AND " + Events.STATUS + " =? ");
                selectionArgs.add(status);
            }

            if (!Utils.isEmpty(startDate)) {
                long startMillis = Utils.parseStrToCld(startDate)
                        .getTimeInMillis();
                selection.append(" AND " + Events.DTSTART + " >=? ");
                selectionArgs.add(startMillis + "");
            }
            if (!Utils.isEmpty(endDate)) {
                long endMillis = Utils.parseStrToCld(endDate).getTimeInMillis();
                selection.append(" AND " + Events.DTEND + " <=? ");
                selectionArgs.add(endMillis + "");
            }
            Log.i(Const.APPTAG, "查询条件:" + selection.toString());
        }
        // EVENTS_COLUMNS 换成 null 查询所有字段
        Cursor eventsCursor = resolver.query(
                eventsUri,
                EVENTS_COLUMNS,
                selection.length() == 0 ? null : selection.toString(),
                selectionArgs.size() == 0 ? null : selectionArgs
                        .toArray(new String[]{}), null);

        Map<String, Object> event = new HashMap<String, Object>();
        while (eventsCursor.moveToNext()) {
            // 以下字段解释，在添加事件里可查看addEvents()
            String eid = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events._ID));
            String calendarId = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.CALENDAR_ID));
            String title = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.TITLE));
            String description = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.DESCRIPTION));
            String location = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.EVENT_LOCATION));
            long startDate = eventsCursor.getLong(eventsCursor
                    .getColumnIndex(Events.DTSTART));
            long endDate = eventsCursor.getLong(eventsCursor
                    .getColumnIndex(Events.DTEND));
            String timeZone = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.EVENT_TIMEZONE));
            String hasAlarm = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.HAS_ALARM));
            String allDay = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.ALL_DAY));
            String availability = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.AVAILABILITY));
            String accessLevel = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.ACCESS_LEVEL));
            String status = eventsCursor.getString(eventsCursor
                    .getColumnIndex(Events.STATUS));

            Calendar calendar = Calendar.getInstance();

            event.put("id", eid);
            event.put("calendarId", calendarId);
            event.put("title", title);
            event.put("description", description);
            event.put("location", location);

            calendar.setTimeInMillis(startDate);
            event.put("startDate", Utils.getFormatCld(calendar));

            calendar.setTimeInMillis(endDate);
            event.put("endDate", Utils.getFormatCld(calendar));

            event.put("timeZone", timeZone);
            event.put("hasAlarm", hasAlarm);
            event.put("allDay", allDay);
            event.put("availability", availability);
            event.put("accessLevel", accessLevel);
            event.put("status", status);
            // 查询提醒
            Cursor remindersCursor = resolver.query(remindersUri,
                    REMINDERS_COLUMNS, Reminders.EVENT_ID + "=?",
                    new String[]{eid}, null);

            List<Map<String, Object>> reminders = new ArrayList<Map<String, Object>>();
            while (remindersCursor.moveToNext()) {
                Map<String, Object> reminder = new HashMap<String, Object>();

                String rid = remindersCursor.getString(remindersCursor
                        .getColumnIndex(Reminders._ID));
                String eventId = remindersCursor.getString(remindersCursor
                        .getColumnIndex(Reminders.EVENT_ID));
                String minutes = remindersCursor.getString(remindersCursor
                        .getColumnIndex(Reminders.MINUTES));
                String method = remindersCursor.getString(remindersCursor
                        .getColumnIndex(Reminders.METHOD));

                reminder.put("id", rid);
                reminder.put("eventId", eventId);
                reminder.put("minutes", minutes);
                reminder.put("method", method);
                reminders.add(reminder);
            }
            remindersCursor.close();
            event.put("reminders", reminders);
            // 查询参与人
            Cursor attendeesCursor = resolver.query(attendeesUri,
                    ATTENDEES_COLUMNS, Attendees.EVENT_ID + "=?",
                    new String[]{eid}, null);

            List<Map<String, Object>> attendees = new ArrayList<Map<String, Object>>();
            while (attendeesCursor.moveToNext()) {
                Map<String, Object> attendee = new HashMap<String, Object>();
                String rid = attendeesCursor.getString(attendeesCursor
                        .getColumnIndex(Attendees._ID));
                String name = attendeesCursor.getString(attendeesCursor
                        .getColumnIndex(Attendees.ATTENDEE_NAME));
                String email = attendeesCursor.getString(attendeesCursor
                        .getColumnIndex(Attendees.ATTENDEE_EMAIL));
                String _status = attendeesCursor.getString(attendeesCursor
                        .getColumnIndex(Attendees.ATTENDEE_STATUS));

                attendee.put("id", rid);
                attendee.put("name", name);
                attendee.put("email", email);
                attendee.put("status", _status);
                attendees.add(attendee);
            }
            attendeesCursor.close();
            event.put("attendees", reminders);

            result.add(event);
        }
        eventsCursor.close();

        return result;
    }

    /**
     * 批量插入日程
     *
     * @param calendars
     * @return Map<String, Object>
     */
    public Map<String, Object> insertEvents(List<Map<String, Object>> calendars) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (Utils.isEmpty(calendars)) {
            result.put("result", "0");
            result.put("obj", "日程信息为空，添加失败！");
            return null;
        }
        List<String> addResult = new ArrayList<String>();
        ArrayList<ContentProviderOperation> ops = null;

        for (int i = 0; i < calendars.size(); i++) {
            // 获得日程
            Map<String, Object> calendar = calendars.get(i);
            // 插入事件
            Uri eUri = null;
            try {
                eUri = insertEvent(calendar);
            } catch (Exception e) {
                addResult.add("第" + (i + 1) + "条日程，添加事件失败：" + e.getMessage());
            }
            // 如果事件插入成功，则插入提醒和参与者
            if (!Utils.isEmpty(eUri)) {
                String eventId = eUri.getLastPathSegment();
                // 存入插入事件的结果
                addResult.add(eUri.toString());

                ops = new ArrayList<ContentProviderOperation>();
                // 插入提醒，可以添加多个提醒
                Map<Object, Map<String, String>> reminders = (Map<Object, Map<String, String>>) calendar
                        .get("reminders");
                if (!Utils.isEmpty(reminders)) {
                    for (Object key : reminders.keySet()) {
                        reminders.get(key).put("eventId", eventId);
                        try {
                            insertReminder(reminders.get(key), ops);
                        } catch (Exception e) {
                            Log.i(Const.APPTAG, e.getMessage());
                        }
                    }
                }
                // 插入参与者，可以添加多个参与者
                Map<Object, Map<String, String>> attendees = (Map<Object, Map<String, String>>) calendar
                        .get("attendees");
                if (!Utils.isEmpty(attendees)) {
                    for (Object key : attendees.keySet()) {
                        attendees.get(key).put("eventId", eventId);
                        try {
                            insertAttendee(attendees.get(key), ops);
                        } catch (Exception e) {
                            Log.i(Const.APPTAG, e.getMessage());
                        }
                    }
                }
                if (!Utils.isEmpty(ops)) {
                    // 执行批量插入
                    try {
                        ContentProviderResult[] cps = resolver.applyBatch(
                                CalendarContract.AUTHORITY, ops);
                        // event表插入返回的Uri集合
                        for (ContentProviderResult cp : cps) {
                            Log.i(Const.APPTAG, cp.toString());
                            addResult.add(cp.uri.toString());
                        }
                    } catch (Exception e) {
                        Log.i(Const.APPTAG, e.getMessage());
                        addResult.add("第" + (i + 1) + "条日程，添加(提醒和参与者)失败:"
                                + e.getMessage());
                    }
                }
            }
        }
        result.put("result", "1");
        result.put("obj", addResult);

        return result;
    }

    /**
     * 插入日程参与者，如果参数ops不为空，则不执行插入，添加到ops里执行批量插入
     *
     * @param attendees
     * @param ops
     * @return Uri
     * @throws Exception
     */
    public Uri insertAttendee(Map<String, String> attendees,
                              ArrayList<ContentProviderOperation> ops) throws Exception {
        if (Utils.isEmpty(attendees)) {
            return null;
        }
        try {
            String eventId = attendees.get("eventId");// 外键事件id
            String name = attendees.get("name");// 参与者姓名
            // 如果时间id、或者参与姓名为空，不添加参与者
            if (!isExistEvent(Long.parseLong(eventId)) || Utils.isEmpty(name)) {
                return null;
            }
            String email = attendees.get("email");// 参与者电子邮件

            /** 没明白具体什么意思，暂时用默认值 */
            int relationship = Attendees.RELATIONSHIP_ATTENDEE;// 与会者与事件的关系
            int type = Attendees.TYPE_OPTIONAL;// 与会者的类型
            int status = Attendees.ATTENDEE_STATUS_INVITED;// 与会者的状态
            if (ops == null) {
                ContentValues attendeesVal = new ContentValues();
                attendeesVal.put(Attendees.EVENT_ID, eventId);
                attendeesVal.put(Attendees.ATTENDEE_NAME, name);
                if (!Utils.isEmpty(email)) {
                    attendeesVal.put(Attendees.ATTENDEE_EMAIL, email);// 参与者
                    // email
                }

                attendeesVal.put(Attendees.ATTENDEE_RELATIONSHIP, relationship);// 关系
                attendeesVal.put(Attendees.ATTENDEE_TYPE, type);// 类型
                attendeesVal.put(Attendees.ATTENDEE_STATUS, status);// 状态

                Uri uri = resolver.insert(attendeesUri, attendeesVal);
                return uri;
            } else {
                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(attendeesUri)
                        .withYieldAllowed(true)
                        .withValue(Attendees.EVENT_ID, eventId)
                        .withValue(Attendees.ATTENDEE_NAME, name)
                        .withValue(Attendees.ATTENDEE_EMAIL, email)
                        .withValue(Attendees.ATTENDEE_RELATIONSHIP,
                                relationship)
                        .withValue(Attendees.ATTENDEE_TYPE, type)
                        .withValue(Attendees.ATTENDEE_STATUS, status);
                if (!Utils.isEmpty(email)) {
                    builder.withValue(Attendees.ATTENDEE_EMAIL, email);
                }
                ops.add(builder.build());
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    /**
     * 插入日程提醒，如果参数ops不为空，则不执行插入。
     *
     * @param reminders
     * @param ops
     * @return Uri
     */
    public Uri insertReminder(Map<String, String> reminders,
                              ArrayList<ContentProviderOperation> ops) throws Exception {
        // ---------------------------Reminders表的数据------------------------------------
        // 插入提醒，可以添加多个提醒
        if (!Utils.isEmpty(reminders)) {
            try {
                String eventId = reminders.get("eventId");// 外键事件id
                // 如果时间id为空，不添加提醒
                if (!isExistEvent(Long.parseLong(eventId))) {
                    return null;
                }

                String mimutes = reminders.get("mimutes");// 提醒在事件前几分钟后发出
                String method = reminders.get("method");// 提醒方法:METHOD_DEFAULT:0,*_ALERT:1,*_EMAIL:2,*_SMS:3

                // 提醒方法
                int methodType = Reminders.METHOD_DEFAULT;
                if (method.equals("1")) {
                    methodType = Reminders.METHOD_ALERT;
                } else if (method.equals("2")) {
                    methodType = Reminders.METHOD_EMAIL;
                } else if (method.equals("3")) {
                    methodType = Reminders.METHOD_SMS;
                }
                // 提醒时间
                int m = Utils.isNumber(mimutes) ? Integer.parseInt(mimutes) : 0;

                if (ops == null) {
                    ContentValues alarmVal = new ContentValues();
                    alarmVal.put(Reminders.EVENT_ID, eventId);
                    alarmVal.put(Reminders.MINUTES, m);// 提醒在事件前多少分钟后发出
                    alarmVal.put(Reminders.METHOD, methodType);

                    Uri uri = resolver.insert(remindersUri, alarmVal);
                    return uri;
                } else {
                    ContentProviderOperation op = ContentProviderOperation
                            .newInsert(remindersUri).withYieldAllowed(true)
                            .withValue(Reminders.EVENT_ID, eventId)
                            .withValue(Reminders.MINUTES, m)
                            .withValue(Reminders.METHOD, methodType).build();
                    ops.add(op);
                }
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    /**
     * 插入日程事件
     *
     * @param events
     * @return Uri
     */
    public Uri insertEvent(Map<String, Object> events) throws Exception {
        if (Utils.isEmpty(events)) {
            return null;
        }
        try {
            ContentValues eventVal = new ContentValues();
            // ---------------------------Event表的数据------------------------------------
            // 插入一条事件，必须满足，有日历id、开始时间、结束时间、和标题或者内容，自定义的，不然插入没有意义
            String calendarId = (String) events.get("calendarId");// 日历id
            String startDate = (String) events.get("startDate");// 开始时间:格式=yyyy-MM-dd
            // HH:mm:ss
            String endDate = (String) events.get("endDate");// 结束时间:格式=yyyy-MM-dd
            // HH:mm:ss
            String title = (String) events.get("title");// 日程标题
            String description = (String) events.get("description");// 日程内容
            if (!Utils.isNumber(calendarId) || Utils.isEmpty(startDate)
                    || Utils.isEmpty(endDate)
                    || (Utils.isEmpty(title) || Utils.isEmpty(description))) {
                return null;
            }

            String location = (String) events.get("location");// 地点
            String timeZone = (String) events.get("timeZone");// 时区：TimeZone.getAvailableIDs()
            String hasAlarm = (String) events.get("hasAlarm");// 是否事件触发报警:0=false,
            // 1=true
            String allDay = (String) events.get("allDay");// 是否全天事件：0=false,
            // 1=true
            // String eventStatus = (String)
            // calendar.get("eventStatus");//事件状态:暂定(0)，确认(1)或取消(2)
            String availability = (String) events.get("availability");// 我的状态:0=忙碌，1=有空
            String accessLevel = (String) events.get("accessLevel");// 访问权限：默认=0，机密=1，私有=2，公共=3

            eventVal.put(Events.CALENDAR_ID, calendarId);
            eventVal.put(Events.TITLE, title);
            eventVal.put(Events.DESCRIPTION, description);
            eventVal.put(Events.STATUS, 1);
            eventVal.put(Events.EVENT_TIMEZONE,
                    Utils.isEmpty(timeZone) ? "Asia/Shanghai" : timeZone);
            eventVal.put(Events.HAS_ALARM,
                    (Utils.isNumber(hasAlarm) ? Integer.parseInt(hasAlarm) : 0));
            // 计算开始、结束时间，全部用Date也可以。
            Calendar cld = Calendar.getInstance();
            Calendar startCld = Calendar.getInstance();
            Calendar endCld = Calendar.getInstance();
            cld = Utils.parseStrToCld(startDate);
            // 如果地址不为空插入地址
            if (!Utils.isEmpty(location)) {
                eventVal.put(Events.EVENT_LOCATION, location);
            }
            // 如果是全天事件的话，取开始时间的那一整天
            if (Utils.isNumber(allDay) && Integer.parseInt(allDay) == 1) {
                // 开始时间
                startCld.set(cld.get(Calendar.YEAR), cld.get(Calendar.MONTH),
                        cld.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                // 结束时间
                endCld.set(cld.get(Calendar.YEAR), cld.get(Calendar.MONTH),
                        cld.get(Calendar.DAY_OF_MONTH), 24, 0, 0);
                eventVal.put(Events.ALL_DAY, true);
                eventVal.put(Events.DTSTART, startCld.getTimeInMillis());
                eventVal.put(Events.DTEND, endCld.getTimeInMillis());
            } else {
                // 开始时间
                startCld = Utils.parseStrToCld(startDate);
                // 结束时间
                endCld = Utils.parseStrToCld(endDate);
                eventVal.put(Events.ALL_DAY, false);
                eventVal.put(Events.DTSTART, startCld.getTimeInMillis());
                eventVal.put(Events.DTEND, endCld.getTimeInMillis());
            }
            // 设置我的状态
            if (Utils.isNumber(availability)
                    && Integer.parseInt(availability) == 0) {
                eventVal.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
            } else {
                eventVal.put(Events.AVAILABILITY, Events.AVAILABILITY_FREE);
            }
            // 设置隐私
            if (Utils.isNumber(accessLevel)
                    && Integer.parseInt(accessLevel) == 1) {
                eventVal.put(CalendarContract.Events.ACCESS_LEVEL,
                        Events.ACCESS_CONFIDENTIAL);
            } else if (Utils.isNumber(accessLevel)
                    && Integer.parseInt(accessLevel) == 2) {
                eventVal.put(CalendarContract.Events.ACCESS_LEVEL,
                        Events.ACCESS_PRIVATE);
            } else if (Utils.isNumber(accessLevel)
                    && Integer.parseInt(accessLevel) == 3) {
                eventVal.put(CalendarContract.Events.ACCESS_LEVEL,
                        Events.ACCESS_PUBLIC);
            } else {
                eventVal.put(CalendarContract.Events.ACCESS_LEVEL,
                        Events.ACCESS_DEFAULT);
            }

            return resolver.insert(eventsUri, eventVal);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 查询Reminder是否存在
     *
     * @param id
     * @return
     */
    public boolean isExistAttendee(long id) {
        Cursor cursor = resolver.query(attendeesUri,
                new String[]{Attendees._ID}, Attendees._ID + "=" + id, null,
                null);
        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
    }

    /**
     * 查询Reminder是否存在
     *
     * @param id
     * @return
     */
    public boolean isExistReminder(long id) {
        Cursor cursor = resolver.query(remindersUri,
                new String[]{Reminders._ID}, Reminders._ID + "=" + id, null,
                null);
        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
    }

    /**
     * 查询event是否存在
     *
     * @param id
     * @return
     */
    public boolean isExistEvent(long id) {
        Cursor cursor = resolver.query(eventsUri, new String[]{Events._ID},
                Events._ID + "=" + id, null, null);
        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
    }

    /**
     * 删除event表里数据
     *
     * @return
     */
    public Map<String, String> delEvents(List<String> ids, String calendarId,
                                         boolean delAll) {
        Map<String, String> result = new HashMap<String, String>();

        String selection = null;

        if (delAll) {
            selection = Events._ID + " > 0";
        } else if (Utils.isNumber(calendarId)) {
            selection = Events.CALENDAR_ID + "=" + calendarId;
        } else if (Utils.isEmpty(ids)) {
            result.put("result", "0");
            result.put("obj", "要删除日程事件的id为空！");
            return result;
        } else {
            String where = "";
            for (String id : ids) {
                if (Utils.isNumber(id)) {
                    where += id + ",";
                }
            }
            selection = Events._ID + " in("
                    + where.substring(0, where.length() - 1) + ")";
        }

        try {
            Log.i(Const.APPTAG, "====：" + selection);
            int n = resolver.delete(eventsUri, selection, null);

            result.put("result", "1");
            result.put("obj", n + "");

        } catch (Exception e) {
            result.put("result", "-1");
            result.put("obj", "删除错误：" + e.toString());
        }
        return result;
    }

    /**
     * 更新日历的名称
     *
     * @param param
     * @return Map
     */
    public Map<String, String> updateCalendars(Map<String, String> param) {
        Map<String, String> result = new HashMap<String, String>();
        if (Utils.isEmpty(param)) {
            result.put("false", "更新参数不能为空！");
            return result;
        }

        String calendarId = param.get("calendarId");
        String displayName = param.get("displayName");

        if (Utils.isEmpty(calendarId) && Utils.isNumber(calendarId)) {
            result.put("false", "日历id不合法！");
            return result;
        }
        if (Utils.isEmpty(displayName)) {
            result.put("false", "日历名称不能为空！");
            return result;
        }

        ContentValues values = new ContentValues();
        values.put(Calendars.CALENDAR_DISPLAY_NAME, displayName);
        Uri uri = ContentUris.withAppendedId(calendarsUri,
                Long.parseLong(calendarId));
        int n = resolver.update(uri, values, null, null);
        result.put("true", n + "");

        return result;
    }

    /**
     * 根据账户查询账户日历
     *
     * @param param Map<String, String>
     * @return List
     */
    public List<Map<String, String>> queryCalendars(Map<String, String> param) {
        String accountName = null;
        String accountType = null;
        String ownerAccount = null;

        if (!Utils.isEmpty(param)) {
            accountName = param.get("accountName");// 账户名称
            accountType = param.get("accountType");// 账户类型
            ownerAccount = param.get("ownerAccount");// 拥有者账户
        }

        List<Map<String, String>> calendars = new ArrayList<Map<String, String>>();

        Cursor cursor = null;
        StringBuffer selection = new StringBuffer(" 1 = 1 ");
        List<String> selectionArgs = new ArrayList<String>();
        // 本地帐户查询：ACCOUNT_TYPE_LOCAL是一个特殊的日历账号类型，它不跟设备账号关联。这种类型的日历不同步到服务器
        // 如果是谷歌的账户是可以同步到服务器的
        if (Utils.isEmpty(accountName) && Utils.isEmpty(accountType)
                && Utils.isEmpty(ownerAccount)) {
            selection.append(" AND " + Calendars.ACCOUNT_TYPE + " = ? ");
            selectionArgs.add("LOCAL");
        } else {
            if (!Utils.isEmpty(accountName)) {
                selection.append(" AND " + Calendars.ACCOUNT_NAME + " = ? ");
                selectionArgs.add(accountName);
            }
            if (!Utils.isEmpty(accountType)) {
                selection.append(" AND " + Calendars.ACCOUNT_TYPE + " = ? ");
                selectionArgs.add(accountType);
            }
            if (!Utils.isEmpty(ownerAccount)) {
                selection.append(" AND " + Calendars.OWNER_ACCOUNT + " = ? ");
                selectionArgs.add(ownerAccount);
            }
        }
        cursor = resolver.query(calendarsUri, CALENDARS_COLUMNS,
                selection.toString(), selectionArgs.toArray(new String[]{}),
                null);
        while (cursor.moveToNext()) {
            Map<String, String> calendar = new HashMap<String, String>();
            // Get the field values
            calendar.put("calendarId", cursor.getString(0));
            calendar.put("accountName", cursor.getString(1));
            calendar.put("displayName", cursor.getString(2));
            calendar.put("ownerAccount", cursor.getString(3));
            Log.i(Const.APPTAG, "查询到日历：" + calendar);
            calendars.add(calendar);
        }
        return calendars;
    }
}
