package github.zimoyin.application.server.ai.pic;

import java.util.List;

/**
 * Auto-generated: 2022-10-31 21:10:15
 *
 * @author json.cn (i@json.cn)
 */
public class Parameters {

    private boolean enable_hr;
    private int denoising_strength;
    private int firstphase_width;
    private int firstphase_height;
    private String prompt;
    private List<String> styles;
    private int seed;
    private int subseed;
    private int subseed_strength;
    private int seed_resize_from_h;
    private int seed_resize_from_w;
    private int batch_size;
    private int n_iter;
    private int steps;
    private int cfg_scale;
    private int width;
    private int height;
    private boolean restore_faces;
    private boolean tiling;
    private String negative_prompt;
    private int eta;
    private int s_churn;
    private int s_tmax;
    private int s_tmin;
    private int s_noise;
    private Override_settings override_settings;
    private String sampler_index;

    public boolean getEnable_hr() {
        return enable_hr;
    }

    public void setEnable_hr(boolean enable_hr) {
        this.enable_hr = enable_hr;
    }

    public int getDenoising_strength() {
        return denoising_strength;
    }

    public void setDenoising_strength(int denoising_strength) {
        this.denoising_strength = denoising_strength;
    }

    public int getFirstphase_width() {
        return firstphase_width;
    }

    public void setFirstphase_width(int firstphase_width) {
        this.firstphase_width = firstphase_width;
    }

    public int getFirstphase_height() {
        return firstphase_height;
    }

    public void setFirstphase_height(int firstphase_height) {
        this.firstphase_height = firstphase_height;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getSubseed() {
        return subseed;
    }

    public void setSubseed(int subseed) {
        this.subseed = subseed;
    }

    public int getSubseed_strength() {
        return subseed_strength;
    }

    public void setSubseed_strength(int subseed_strength) {
        this.subseed_strength = subseed_strength;
    }

    public int getSeed_resize_from_h() {
        return seed_resize_from_h;
    }

    public void setSeed_resize_from_h(int seed_resize_from_h) {
        this.seed_resize_from_h = seed_resize_from_h;
    }

    public int getSeed_resize_from_w() {
        return seed_resize_from_w;
    }

    public void setSeed_resize_from_w(int seed_resize_from_w) {
        this.seed_resize_from_w = seed_resize_from_w;
    }

    public int getBatch_size() {
        return batch_size;
    }

    public void setBatch_size(int batch_size) {
        this.batch_size = batch_size;
    }

    public int getN_iter() {
        return n_iter;
    }

    public void setN_iter(int n_iter) {
        this.n_iter = n_iter;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getCfg_scale() {
        return cfg_scale;
    }

    public void setCfg_scale(int cfg_scale) {
        this.cfg_scale = cfg_scale;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean getRestore_faces() {
        return restore_faces;
    }

    public void setRestore_faces(boolean restore_faces) {
        this.restore_faces = restore_faces;
    }

    public boolean getTiling() {
        return tiling;
    }

    public void setTiling(boolean tiling) {
        this.tiling = tiling;
    }

    public String getNegative_prompt() {
        return negative_prompt;
    }

    public void setNegative_prompt(String negative_prompt) {
        this.negative_prompt = negative_prompt;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public int getS_churn() {
        return s_churn;
    }

    public void setS_churn(int s_churn) {
        this.s_churn = s_churn;
    }

    public int getS_tmax() {
        return s_tmax;
    }

    public void setS_tmax(int s_tmax) {
        this.s_tmax = s_tmax;
    }

    public int getS_tmin() {
        return s_tmin;
    }

    public void setS_tmin(int s_tmin) {
        this.s_tmin = s_tmin;
    }

    public int getS_noise() {
        return s_noise;
    }

    public void setS_noise(int s_noise) {
        this.s_noise = s_noise;
    }

    public Override_settings getOverride_settings() {
        return override_settings;
    }

    public void setOverride_settings(Override_settings override_settings) {
        this.override_settings = override_settings;
    }

    public String getSampler_index() {
        return sampler_index;
    }

    public void setSampler_index(String sampler_index) {
        this.sampler_index = sampler_index;
    }

}