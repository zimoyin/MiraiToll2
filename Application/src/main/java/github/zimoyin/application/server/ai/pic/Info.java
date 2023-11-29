package github.zimoyin.application.server.ai.pic;

import java.util.List;

/**
 * Auto-generated: 2022-10-31 21:10:15
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Info {

    private String prompt;
    private List<String> all_prompts;
    private String negative_prompt;
    private long seed;
    private List<Long> all_seeds;
    private long subseed;
    private List<Long> all_subseeds;
    private int subseed_strength;
    private int width;
    private int height;
    private int sampler_index;
    private String sampler;
    private int cfg_scale;
    private int steps;
    private int batch_size;
    private boolean restore_faces;
    private String face_restoration_model;
    private String sd_model_hash;
    private int seed_resize_from_w;
    private int seed_resize_from_h;
    private int denoising_strength;
    private Extra_generation_params extra_generation_params;
    private int index_of_first_image;
    private List<String> infotexts;
    private List<String> styles;
    private String job_timestamp;
    private int clip_skip;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getAll_prompts() {
        return all_prompts;
    }

    public void setAll_prompts(List<String> all_prompts) {
        this.all_prompts = all_prompts;
    }

    public String getNegative_prompt() {
        return negative_prompt;
    }

    public void setNegative_prompt(String negative_prompt) {
        this.negative_prompt = negative_prompt;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public List<Long> getAll_seeds() {
        return all_seeds;
    }

    public void setAll_seeds(List<Long> all_seeds) {
        this.all_seeds = all_seeds;
    }

    public long getSubseed() {
        return subseed;
    }

    public void setSubseed(long subseed) {
        this.subseed = subseed;
    }

    public List<Long> getAll_subseeds() {
        return all_subseeds;
    }

    public void setAll_subseeds(List<Long> all_subseeds) {
        this.all_subseeds = all_subseeds;
    }

    public int getSubseed_strength() {
        return subseed_strength;
    }

    public void setSubseed_strength(int subseed_strength) {
        this.subseed_strength = subseed_strength;
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

    public int getSampler_index() {
        return sampler_index;
    }

    public void setSampler_index(int sampler_index) {
        this.sampler_index = sampler_index;
    }

    public String getSampler() {
        return sampler;
    }

    public void setSampler(String sampler) {
        this.sampler = sampler;
    }

    public int getCfg_scale() {
        return cfg_scale;
    }

    public void setCfg_scale(int cfg_scale) {
        this.cfg_scale = cfg_scale;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getBatch_size() {
        return batch_size;
    }

    public void setBatch_size(int batch_size) {
        this.batch_size = batch_size;
    }

    public boolean getRestore_faces() {
        return restore_faces;
    }

    public void setRestore_faces(boolean restore_faces) {
        this.restore_faces = restore_faces;
    }

    public String getFace_restoration_model() {
        return face_restoration_model;
    }

    public void setFace_restoration_model(String face_restoration_model) {
        this.face_restoration_model = face_restoration_model;
    }

    public String getSd_model_hash() {
        return sd_model_hash;
    }

    public void setSd_model_hash(String sd_model_hash) {
        this.sd_model_hash = sd_model_hash;
    }

    public int getSeed_resize_from_w() {
        return seed_resize_from_w;
    }

    public void setSeed_resize_from_w(int seed_resize_from_w) {
        this.seed_resize_from_w = seed_resize_from_w;
    }

    public int getSeed_resize_from_h() {
        return seed_resize_from_h;
    }

    public void setSeed_resize_from_h(int seed_resize_from_h) {
        this.seed_resize_from_h = seed_resize_from_h;
    }

    public int getDenoising_strength() {
        return denoising_strength;
    }

    public void setDenoising_strength(int denoising_strength) {
        this.denoising_strength = denoising_strength;
    }

    public Extra_generation_params getExtra_generation_params() {
        return extra_generation_params;
    }

    public void setExtra_generation_params(Extra_generation_params extra_generation_params) {
        this.extra_generation_params = extra_generation_params;
    }

    public int getIndex_of_first_image() {
        return index_of_first_image;
    }

    public void setIndex_of_first_image(int index_of_first_image) {
        this.index_of_first_image = index_of_first_image;
    }

    public List<String> getInfotexts() {
        return infotexts;
    }

    public void setInfotexts(List<String> infotexts) {
        this.infotexts = infotexts;
    }

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public String getJob_timestamp() {
        return job_timestamp;
    }

    public void setJob_timestamp(String job_timestamp) {
        this.job_timestamp = job_timestamp;
    }

    public int getClip_skip() {
        return clip_skip;
    }

    public void setClip_skip(int clip_skip) {
        this.clip_skip = clip_skip;
    }

}