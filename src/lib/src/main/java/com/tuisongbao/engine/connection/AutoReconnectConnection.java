package com.tuisongbao.engine.connection;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

/**
 * <STRONG>自动连接管理类</STRONG>
 *
 * <P>
 * 继承于 {@link Connection}，不同的是，当连接断开时，该类会进行自动重连。
 *
 * @author Katherine Zhu
 * @version v2.1.0
 */
public class AutoReconnectConnection extends Connection {
    private static final String TAG = "TSB" + AutoReconnectConnection.class.getSimpleName();
    /**
     * 重连间隔
     */
    private int mReconnectGap = 0;
    /**
     * 重连次数
     */
    private long mReconnectTimes = 0;
    /**
     * 重连策略
     */
    private String mReconnectStrategy = Protocol.CONNECTION_STRATEGY_BACKOFF;
    /**
     * 重连基数
     */
    private int mReconnectIn = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN;
    /**
     * 重连最大间隔
     */
    private int mReconnectMax = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN_MAX;
    private int mConnectionType = Protocol.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY;

    public AutoReconnectConnection(Engine engine) {
        super(engine);
    }

    @Override
    public void connect() {
        backoff();
        super.connect();
    }

    @Override
    protected void handleConnectionEvent(String eventName, ConnectionEventData data) {
        super.handleConnectionEvent(eventName, data);
        if (StrUtils.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ERROR)) {
            int code = lastConnectionError.getCode();
            // 4000 ~ 4099: 连接将被服务端关闭, 客户端 不 应该进行重连。
            if (code >= 4000 && code <= 4099) {
                mConnectionType = Protocol.CONNECTION_STRATEGY_CONNECTION_TYPE_FORBIDDEN_CONNECTION;
                updateState(State.Failed);
                trigger(Connection.EVENT_ERROR, lastConnectionError.getMessage());
                stop();
            }
            // 4100 ~ 4199: 连接将被服务端关闭, 客户端应按照指示进行重连。
            if (code >= 4100 && code <= 4199) {
                mConnectionType = Protocol.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY;
                setReconnectionStrategy(data);
                reconnect();
            }
        }
    }

    @Override
    protected void onSocketClosed(Object... args) {
        super.onSocketClosed(args);
        updateState(State.Connecting);
        resetConnectionStrategy();
        reconnect();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        resetConnectionStrategy();
    }

    private void setReconnectionStrategy(ConnectionEventData data) {
        String reconnectStrategy = data.getReconnectStrategy();
        if (!StrUtils.isEmpty(reconnectStrategy)) {
            mReconnectStrategy = reconnectStrategy;
            int reconnectIn = data.getReconnectIn();
            if (reconnectIn > 0) {
                mReconnectIn = reconnectIn;
            }
            int reconnectMax = data.getReconnectInMax();
            if (reconnectMax > 0) {
                mReconnectMax = reconnectMax;
            }
        }
    }

    /**
     * 该方法用于控制重连频率，在重连网络之前需要判断其需要经个多少再连一次
     */
    private void backoff() {
        // 需要马上重连
        if (mConnectionType == Protocol.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_IMMEDIATELY
                && mReconnectTimes <= 0) {
            mReconnectTimes++;
            return;
        }
        if (Protocol.CONNECTION_STRATEGY_STATIC.equals(mReconnectStrategy)) {
            /**
             * static ：以静态的间隔进行重连，服务端可以通过 engine_connection:error ConnectionEvent 的
             * data.reconnectStrategy 来启用，通过 data.reconnectIn 设置重连间隔。
             */
            if (mReconnectIn <= 0) {
                mReconnectIn = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN_MAX;
            }
            mReconnectGap = mReconnectIn;
        } else {
            /**
             * backoff ：默认策略，重连间隔从一个基数开始（默认为 0），每次乘以 2 ，直到达到最大值（默认为 10 秒）。服务端可以通过
             * engine_connection:error ConnectionEvent 的 data.reconnectIn 、 data.reconnectInMax
             * 来调整基数和最大值，当然对应的 data.reconnectStrategy 需为 backoff 。
             *
             * 以默认值为例，不断自动重连时，间隔将依次为（单位秒）：1 2 4 8 10 10 10....
             */
            if (mReconnectMax <= 0) {
                mReconnectMax = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN_MAX;
            }
            if (mReconnectIn < 0) {
                mReconnectIn = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN;
            }
            if (mReconnectTimes <= 0) {
                mReconnectGap = mReconnectIn;
            } else {
                if(mReconnectGap * 2 < mReconnectMax) {
                    mReconnectGap = mReconnectGap * 2;
                } else {
                    mReconnectGap = mReconnectMax;
                }
            }
        }
        try {
            LogUtils.verbose(TAG, "Start to sleep： " + mReconnectGap);
            trigger(EVENT_CONNECTING_IN, mReconnectGap);
            if (mReconnectGap > 0) {
                Thread.sleep(mReconnectGap * 1000);
            }
            LogUtils.verbose(TAG, "End to sleep： " + mReconnectGap);
        } catch (Exception e) {
            LogUtils.error(TAG, "Connection sleep exception", e);
        }
        mReconnectTimes++;
    }

    private void resetConnectionStrategy() {
        mReconnectGap = 1;
        mReconnectTimes = 0;
        mReconnectStrategy = Protocol.CONNECTION_STRATEGY_BACKOFF;
        mReconnectIn = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN;
        mReconnectMax = Protocol.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECT_IN_MAX;
        mConnectionType = Protocol.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY;
    }
}
