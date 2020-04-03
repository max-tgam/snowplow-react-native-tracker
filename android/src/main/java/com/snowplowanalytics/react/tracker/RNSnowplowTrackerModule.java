
package com.snowplowanalytics.react.tracker;

import java.util.UUID;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.snowplowanalytics.react.util.EventUtil;
import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing;
import com.snowplowanalytics.snowplow.tracker.events.Structured;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;

public class RNSnowplowTrackerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Tracker tracker;
    private Emitter emitter;

    public RNSnowplowTrackerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNSnowplowTracker";
    }

    @ReactMethod
    public void initialize(String endpoint, String method, String protocol,
                           String namespace, String appId, ReadableMap options) {
        this.emitter = new Emitter.EmitterBuilder(endpoint, this.reactContext)
                .method(method.equalsIgnoreCase("post") ? HttpMethod.POST : HttpMethod.GET)
                .security(protocol.equalsIgnoreCase("https") ? RequestSecurity.HTTPS : RequestSecurity.HTTP)
                .build();
        this.emitter.waitForEventStore();
        com.snowplowanalytics.snowplow.tracker.Subject subject = new com.snowplowanalytics.snowplow.tracker.Subject.SubjectBuilder()
                .build();
        if (options.hasKey("userId") && options.getString("userId") != null && !options.getString("userId").isEmpty()) {
            subject.setUserId(options.getString("userId"));
        }
        if (options.hasKey("screenWidth") && options.hasKey("screenHeight")) {
            subject.setScreenResolution(options.getInt("screenWidth"), options.getInt("screenHeight"));
        }
        if (options.hasKey("colorDepth")) {
            subject.setColorDepth(options.getInt("colorDepth"));
        }
        if (options.hasKey("timezone") && options.getString("timezone") != null
                && !options.getString("timezone").isEmpty()) {
            subject.setTimezone(options.getString("timezone"));
        }
        if (options.hasKey("language") && options.getString("language") != null
                && !options.getString("language").isEmpty()) {
            subject.setLanguage(options.getString("language"));
        }
        if (options.hasKey("ipAddress") && options.getString("ipAddress") != null
                && !options.getString("ipAddress").isEmpty()) {
            subject.setIpAddress(options.getString("ipAddress"));
        }
        if (options.hasKey("useragent") && options.getString("useragent") != null
                && !options.getString("useragent").isEmpty()) {
            subject.setUseragent(options.getString("useragent"));
        }
        if (options.hasKey("networkUserId") && options.getString("networkUserId") != null
                && !options.getString("networkUserId").isEmpty()) {
            subject.setNetworkUserId(options.getString("networkUserId"));
        }
        if (options.hasKey("domainUserId") && options.getString("domainUserId") != null
                && !options.getString("domainUserId").isEmpty()) {
            subject.setDomainUserId(options.getString("domainUserId"));
        }
        this.tracker = Tracker.init(new Tracker
                .TrackerBuilder(this.emitter, namespace, appId, this.reactContext)
                .base64(false)
                .mobileContext(true)
                .subject(subject)
                .screenviewEvents(options.hasKey("autoScreenView") ? options.getBoolean("autoScreenView") : false)
                .build()
        );
    }

    @ReactMethod
    public void trackSelfDescribingEvent(ReadableMap event, ReadableArray contexts) {
        SelfDescribing trackerEvent = EventUtil.getSelfDescribingEvent(event, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
        }
    }

    @ReactMethod
    public void trackStructuredEvent(String category, String action, String label,
                                     String property, Float value, ReadableArray contexts) {
        Structured trackerEvent = EventUtil.getStructuredEvent(category, action, label,
                property, value, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
        }
    }

    @ReactMethod
    public void trackScreenViewEvent(String screenName, String screenId, String screenType,
                                     String previousScreenName, String previousScreenType,
                                     String previousScreenId, String transitionType,
                                     ReadableArray contexts) {
        if (screenId == null) {
          screenId = UUID.randomUUID().toString();
        }
        ScreenView trackerEvent = EventUtil.getScreenViewEvent(screenName,
                screenId, screenType, previousScreenName, previousScreenId, previousScreenType,
                transitionType, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
        }
    }
    
    @ReactMethod
    public void setUserId(String userId) {
        tracker.instance().getSubject().setUserId(userId);
    }
}
