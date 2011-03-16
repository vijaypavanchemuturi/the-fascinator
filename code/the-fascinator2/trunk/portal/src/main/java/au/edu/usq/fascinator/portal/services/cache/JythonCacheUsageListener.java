/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.usq.fascinator.portal.services.cache;

import net.sf.ehcache.statistics.CacheUsageListener;

/**
 *
 * @author lucido
 */
public class JythonCacheUsageListener implements CacheUsageListener {

    @Override
    public void notifyStatisticsEnabledChanged(boolean enabled) {
    }

    @Override
    public void notifyStatisticsCleared() {
    }

    @Override
    public void notifyCacheHitInMemory() {
    }

    @Override
    public void notifyCacheHitOffHeap() {
    }

    @Override
    public void notifyCacheHitOnDisk() {
    }

    @Override
    public void notifyCacheElementPut() {
    }

    @Override
    public void notifyCacheElementUpdated() {
    }

    @Override
    public void notifyCacheMissedWithNotFound() {
    }

    @Override
    public void notifyCacheMissInMemory() {
    }

    @Override
    public void notifyCacheMissOffHeap() {
    }

    @Override
    public void notifyCacheMissOnDisk() {
    }

    @Override
    public void notifyCacheMissedWithExpired() {
    }

    @Override
    public void notifyTimeTakenForGet(long time) {
    }

    @Override
    public void notifyCacheElementEvicted() {
    }

    @Override
    public void notifyCacheElementExpired() {
    }

    @Override
    public void notifyCacheElementRemoved() {
    }

    @Override
    public void notifyRemoveAll() {
    }

    @Override
    public void notifyStatisticsAccuracyChanged(int accuracy) {
    }

    @Override
    public void dispose() {
    }
}
