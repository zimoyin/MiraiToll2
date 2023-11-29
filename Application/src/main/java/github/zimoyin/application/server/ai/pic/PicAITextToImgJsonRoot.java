/**
 * Copyright 2022 json.cn
 */
package github.zimoyin.application.server.ai.pic;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-generated: 2022-10-31 20:12:10
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Getter
@Setter
public class PicAITextToImgJsonRoot {

    private boolean enable_hr = false;
    private int width = 512;
    private int height = 512;
    private int denoising_strength = 0;
    private int firstphase_width = 0;
    private int firstphase_height = 0;
    private int steps = 20;
    private String prompt = "";
    private String negative_prompt = "lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, username, blurry";
    private List<String> styles = new ArrayList<>();
    private int seed = -1;
    private int subseed = -1;
    private int subseed_strength = 0;
    private int seed_resize_from_h = -1;
    private int seed_resize_from_w = -1;
    private int batch_size = 1;
    private int n_iter = 1;
    private int cfg_scale = 7;
    private boolean restore_faces = false;
    private boolean tiling = false;
    private int eta = 0;
    private int s_churn = 0;
    private int s_tmax = 0;
    private int s_tmin = 0;
    private int s_noise = 1;
    private JSONObject override_settings = new JSONObject();
    private String sampler_index = "Euler";


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}