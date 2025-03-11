package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.response;

import com.google.gson.JsonElement;

/**
 * Base response model for ZingMP3 API
 */
public class BaseResponse {
    private int err;
    private String msg;
    private JsonElement data;
    private long timestamp;

    public int getErr() {
        return err;
    }

    public void setErr(int err) {
        this.err = err;
    }

    public String getMsg() {
        return msg != null ? msg : "";
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return err == 0 && data != null;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "err=" + err +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}