#version 150

uniform sampler2D sphereTexture00;
uniform sampler2D sphereTexture01;
uniform sampler2D sphereTexture10;
uniform sampler2D sphereTexture11;

uniform sampler2D atmTexture;

uniform float sphereBrightness;
uniform float atmBrightness;

uniform int scrWidth;
uniform int scrHeight;

out vec4 fragColor;

const float overallBrightness = 2.0; 

uniform int divs;

float getDiv3(float var) {
	if (var < (1.0/3.0)) {
		var = var*3.0;
	} else if (var < (2.0/3.0)) {
		var = (var - (1.0/3.0)) * 3.0;
	} else {
		var = (var - (2.0/3.0)) * 3.0;
	}

	return var;
}

float getDiv2(float var) {
	if (var < (1.0/2.0)) {
		var = var*2.0;
	} else {
		var = (var - (1.0/2.0)) * 2.0;
	}

	return var;
}

void main() {
	float width = float(scrWidth);
	float height = float(scrHeight);
	
	float x = gl_FragCoord.x/width;
	float y = gl_FragCoord.y/height;
	
	vec4 sphereColor;
  	vec4 atmColor;
	if (divs == 3) {
		x = getDiv3(x);
		y = getDiv3(y);
		vec2 tCoord = vec2(x,y);		
		
	} else if (divs == 2) {
		vec2 tCoord;
		
		if (x < (1.0/2.0)) {
			if (y < (1.0/2.0)) {
				tCoord = vec2(x*2.0,y*2.0);
				sphereColor = vec4(texture(sphereTexture00, tCoord).rgb, 1.0);	  			
			} else {
				tCoord = vec2(x*2.0,(y - (1.0/2.0)) * 2.0);
				sphereColor = vec4(texture(sphereTexture10, tCoord).rgb, 1.0);
			}
		} else {
			if (y < (1.0/2.0)) {
				tCoord = vec2((x - (1.0/2.0)) * 2.0,y*2.0);
				sphereColor = vec4(texture(sphereTexture01, tCoord).rgb, 1.0);
			} else {
				tCoord = vec2((x - (1.0/2.0)) * 2.0,(y - (1.0/2.0)) * 2.0);
				sphereColor = vec4(texture(sphereTexture11, tCoord).rgb, 1.0);
			}
		}
		atmColor  = vec4(texture(atmTexture, tCoord).rgb, 1.0);
	}	
    
    vec4 color = mix(sphereColor * sphereBrightness, atmColor * atmBrightness, 0.5); 
    
    fragColor = vec4(color.rgb * overallBrightness, 1.0);
}
