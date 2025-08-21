package github.poscard8.peritia.xpsource.data;

import com.google.gson.JsonObject;
import github.poscard8.peritia.xpsource.DataXpSource;

import java.util.Map;

public abstract sealed class XpSourceData permits ServerXpSourceData, ClientXpSourceData
{
    Map<DataXpSource, JsonObject> dataMap;

    protected XpSourceData(Map<DataXpSource, JsonObject> dataMap) { this.dataMap = dataMap; }

    public Map<DataXpSource, JsonObject> dataMap() { return dataMap; }

    public void loadXpSource(DataXpSource xpSource)
    {
        xpSource.loadData(dataMap().getOrDefault(xpSource, new JsonObject()));
    }

}
