#version 150

uniform sampler2D DiffuseSampler;
uniform float FearProgress;
uniform float HeartbeatPhase;

in vec2 texCoord;
out vec4 fragColor;

void main()
{
    vec4 original = texture(DiffuseSampler, texCoord);

    if (FearProgress <= 0.0)
    {
        fragColor = original;
        return;
    }

    vec2 center = vec2(0.5);
    vec2 dir = texCoord - center;
    float dist = length(dir);

    // Этап 1: Первый "бух" (lub)
    // Резкий подъем 0..0.1, плавный спад 0.1..0.3
    float p1 = smoothstep(0.0, 0.1, HeartbeatPhase) * (1.0 - smoothstep(0.1, 0.3, HeartbeatPhase));
    
    // Этап 2: Второй "бух" (dub) - самый сильный
    // Резкий подъем 0.2..0.3, очень длинное и плавное затухание 0.3..1.0
    float p2 = smoothstep(0.2, 0.3, HeartbeatPhase) * (1.0 - smoothstep(0.3, 1.0, HeartbeatPhase));

    // Усиливаем резкость пульсаций (искривляем график для резкости удара)
    p1 = pow(p1, 1.2);
    p2 = pow(p2, 1.5);

    // Объединенный пульс. Первый удар дает 60% силы, второй 100%.
    float pulse = max(p1 * 0.6, p2);

    // "Отходняк" (receding level). 
    // Чем выше страх (FearProgress), тем меньше эффект отступает назад.
    // На максимуме страха пульс падает только до 0.65 (то есть отступает на 35% от максимума).
    float rest_level = FearProgress * 0.65;
    
    // Итоговый пульс (колеблется от rest_level до 1.0)
    float final_pulse = mix(rest_level, 1.0, pulse);

    // Общая сила эффекта (с учетом общего уровня страха)
    float strength = final_pulse * FearProgress;

    // Радиус порога, с которого начинается блюр. Стягивается к центру (до 0.3) при сильном ударе.
    float threshold = 0.75 - (0.45 * final_pulse) * FearProgress;
    
    // Плавность границы блюра
    float softness = 0.25;
    
    // Маска виньетки: 0 в центре, 1 по краям (где dist > threshold)
    float blur_mask = smoothstep(threshold - softness, threshold + softness, dist);

    // Радиальный блюр
    float blur = blur_mask * strength * 0.08;

    vec4 sum = vec4(0.0);
    const int samples = 14;

    for(int i = 0; i < samples; i++)
    {
        float t = float(i) / float(samples - 1);
        
        // Стягиваем координаты к центру в зависимости от силы блюра
        vec2 uv = center + dir * (1.0 - blur * t);
        
        sum += texture(DiffuseSampler, uv);
    }

    vec4 blurred = sum / float(samples);

    // Красная дымка (кровь/страх)
    vec3 red = vec3(0.5, 0.0, 0.0);
    
    // Смешиваем оригинальный цвет с блюром и добавляем красную дымку
    vec3 color = mix(original.rgb, blurred.rgb, blur_mask * strength);
    color = mix(color, red, blur_mask * strength * 0.4);

    fragColor = vec4(color, 1.0);
}