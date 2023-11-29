package github.zimoyin.application.command.chatgpt.api.cofig;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.mtool.config.application.ApplicationConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Getter
public final class ChatGPTConfig3 extends ApplicationConfig {
    /**
     * 完成时生成的最大回答字数。
     * 您的提示加上的令牌计数max_tokens不能超过模型的上下文长度。大多数模型的上下文长度为 2048 个标记（最新模型除外，它支持 4096）。
     */
    private int max_tokens = 2024;
    /**
     * 是否回流部分进度。如果设置，令牌将在可用时作为仅数据服务器发送事件发送，流由data: [DONE]消息终止。
     */
    private boolean stream = false;
    /**
     * 除了完成之外回显提示
     */
//    private boolean echo = false;
    private String model = "gpt-3.5-turbo";
    private String key = "please enter key";
    /**
     * 介于 0 和 2 之间。较高的值（如 0.8）将使输出更加随机，而较低的值（如 0.2）将使输出更加集中和确定。
     * 我们通常建议改变这个或top_p但不是两者。
     */
    private double temperature = 0;

    /**
     * 一种替代温度采样的方法，称为核采样，其中模型考虑具有 top_p 概率质量的标记的结果。所以 0.1 意味着只考虑构成前 10% 概率质量的标记。
     * 我们通常建议改变这个或temperature但不是两者。
     */
//    private double top_p = 1;

    /**
     * 后缀: 插入文本完成后出现的后缀。
     */
//    private String suffix = "";
    /**
     * 要说的话
     * 生成完成的提示，编码为字符串、字符串数组、标记数组或标记数组数组。
     * 请注意，<|endoftext|> 是模型在训练期间看到的文档分隔符，因此如果未指定提示，模型将生成新文档的开头。
     */
    @Setter
    private JSONArray messages = JSONArray.parse("[\"Please repeat this sentence. You did not specify any question content\"]");
    /**
     * 为每个提示生成多少完成。
     * 注意：因为这个参数会产生很多完成，它会很快消耗你的令牌配额。请谨慎使用并确保您对max_tokens和进行了合理的设置stop。
     */
//    private int n = 1;

    /**
     * -2.0 和 2.0 之间的数字。正值会根据到目前为止是否出现在文本中来惩罚新标记，从而增加模型谈论新主题的可能性。
     */
//    private double presence_penalty = 0.0;
    /**
     * -2.0 和 2.0 之间的数字。正值会根据新标记在文本中的现有频率对其进行惩罚，从而降低模型逐字重复同一行的可能性。
     */
//    private double frequency_penalty = 0.0;
    /**
     * best_of在服务器端生成完成并返回“最佳”（每个标记具有最高对数概率的那个）。无法流式传输结果。
     * 与 一起使用时n，best_of控制候选完成的数量并n指定要返回的数量 -best_of必须大于n。
     * 注意：因为这个参数会产生很多完成，它会很快消耗你的令牌配额。请谨慎使用并确保您对max_tokens和进行了合理的设置stop。
     */
//    private int best_of = 1;
    /**
     * 修改指定标记出现在完成中的可能性。
     * 接受一个 json 对象，该对象将标记（由 GPT 标记器中的标记 ID 指定）映射到从 -100 到 100 的相关偏差值。您可以使用此标记器工具（适用于 GPT-2 和 GPT-3）来转换文本到令牌 ID。从数学上讲，偏差会在采样之前添加到模型生成的对数中。确切的效果因模型而异，但 -1 和 1 之间的值应该会减少或增加选择的可能性；像 -100 或 100 这样的值应该导致相关令牌的禁止或独占选择。
     * 例如，您可以传递{"50256": -100}以防止生成 <|endoftext|> 标记。
     */
//    private String logit_bias = "";
    /**
     * 唯一标识ID
     */
    private String user = "root";


    public ChatGPTConfig3() {
        super(true);
        initialize();
    }

    public ChatGPTConfig3(boolean isInit) {
        super(isInit);
        if (isInit) initialize();
    }

    private void initialize() {
//        List<Field> fields = getFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            try {
//                field.set(this, get(field.getName()));
//            } catch (IllegalAccessException e) {
//                log.warn("无法为 {}类的{}字段的进行注入值", this.getClass().getCanonicalName(), field.getName());
//            }
//        }
        setFieldValue(this);
    }


    @Override
    public void save() throws IOException {
        //TODO ApplicationConfig 当保存的时候读取对象内的值然后再保存
        super.save();
    }

    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
        this.put("max_tokens", max_tokens);
    }

    public void setStream(boolean stream) {
        this.stream = stream;
        this.put("stream", String.valueOf(stream));
    }


    public void setModel(String model) {
        this.model = model;
        this.put("model", String.valueOf(model));
    }

    public void setKey(String key) {
        this.key = key;
        this.put("key", String.valueOf(key));
    }

    public void setUser(String user) {
        this.user = user;
        this.put("user", user);
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.put("temperature", temperature);
    }

    public void setMessages(JSONArray messages) {
        this.messages = messages;
        this.put("messages", messages);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        this.forEach((key, value) -> {
            if (key != null && value != null && !key.isEmpty() && !value.toString().isEmpty()) json.put(key, value);
        });
        json.remove("key");
        return json;
    }

    @Override
    public ChatGPTConfig3 clone() {
        return (ChatGPTConfig3) super.clone();
    }
}
