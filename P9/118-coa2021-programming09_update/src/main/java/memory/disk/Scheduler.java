package memory.disk;

public class Scheduler {

    /**
     * 先来先服务算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        // TODO
        int track = start;
        double tracks_sum = 0;
        for (int i = 0; i < request.length; ++i)
        {
            tracks_sum += Math.abs(request[i] - track);
            track = request[i];
        }
        return tracks_sum / request.length;
    }

    /**
     * 最短寻道时间优先算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        // TODO
        int track = start;
        double tracks_sum = 0;
        int min_track_no = 0;
        for(int i = 0; i < request.length; ++i)
        {
            for(int j = 0; j <request.length; ++j)
            {
                if(request[j] == -1) continue;
                if(Math.abs(request[min_track_no] - track) > Math.abs(request[j] - track))
                    min_track_no = j;
            }
            tracks_sum += Math.abs(request[min_track_no] - track);
            track = request[min_track_no];
            request[min_track_no] = -1;
            //重置 min_track_no
            min_track_no = 0;
            while (min_track_no < request.length && request[min_track_no] == -1) min_track_no++;
        }
        return tracks_sum / request.length;
    }

    /**
     * 扫描算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        // TODO
        //对请求排序
        for(int i = 0; i < request.length; ++i)
        {
            for(int j = 0; j < request.length - 1; ++j)
            {
                if(request[j] > request[j + 1])
                {
                    int temp = request[j];
                    request[j] = request[j + 1];
                    request[j + 1] = temp;
                }
            }
        }

        int track = start;
        int track_num = Disk.getDisk().get_track_num();
        double tracks_sum = 0;

        if(direction)
        {
            if(request[0] >= track) tracks_sum += request[request.length-1] - track;
            else tracks_sum += (track_num - 1) - track + (track_num - 1) - request[0];
        }
        else
        {
            if(request[request.length-1] <= track) tracks_sum += track - request[0];
            else tracks_sum += track + request[request.length-1];
        }
        return tracks_sum / request.length;
    }

}
