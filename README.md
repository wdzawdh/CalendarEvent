#### 通过系统日历来提示用户的方案。还是希望能帮到其他有类似需求的小伙伴，要是真帮到了希望能来个Star。

### 其实说白了就是通过ContentResolver给日历相关的数据库insert数据，那么系统就会根据你的要求来提示用户，就是这么简单。
## 使用方式（增删改查）
```
public class EventModel {
    private String id;
    private String time;
    private String content;
    ...
}

CalendarEvent.insertEvent(eventModel);
CalendarEvent.deleteEvent(id);
CalendarEvent.deleteAllEvent();
CalendarEvent.updateEvent(eventModel);
CalendarEvent.queryEvents()
```
## 其他的骚操作
#### 其实可以设置的参数非常的多，我暂时只是用涉及到了提示时间和提示内容，还有的操作需要小伙伴们自己发掘了，因为能搜到的栗子确实不多，我把一个比较有参考意义的类放在help包下，希望会对小伙伴有所帮助。
>虽然不知道是谁写的，但挺有参考意义的。 https://github.com/wdzawdh/EventLog/blob/master/app/src/main/java/help/CalendarsResolver.java
