#version 150

uniform sampler2D sphereTextureLT;
uniform sampler2D sphereTextureRT;
uniform sampler2D sphereTextureLB;
uniform sampler2D sphereTextureRB;

uniform sampler2D atmTexture;

uniform float sphereBrightness;
uniform float atmBrightness;

uniform int scrWidth;
uniform int scrHeight;

out vec4 fragColor;

const float overallBrightness = 2.0; 

uniform int divs;
uniform int selection;

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
				sphereColor = vec4(texture(sphereTextureLB, tCoord).rgb, 1.0);	  			
			} else {
				tCoord = vec2(x*2.0,(y - (1.0/2.0)) * 2.0);
				sphereColor = vec4(texture(sphereTextureLT, tCoord).rgb, 1.0);
			}
		} else {
			if (y < (1.0/2.0)) {
				tCoord = vec2((x - (1.0/2.0)) * 2.0,y*2.0);
				sphereColor = vec4(texture(sphereTextureRB, tCoord).rgb, 1.0);
			} else {
				tCoord = vec2((x - (1.0/2.0)) * 2.0,(y - (1.0/2.0)) * 2.0);
				sphereColor = vec4(texture(sphereTextureRT, tCoord).rgb, 1.0);
			}
		}
		
		atmColor  = vec4(texture(atmTexture, tCoord).rgb, 1.0);
	} else {
		vec2 tCoord = vec2(x,y);
		if (selection == 1) {
			sphereColor = vec4(texture(sphereTextureLT, tCoord).rgb, 1.0);
		} else if (selection == 2) {
			sphereColor = vec4(texture(sphereTextureRT, tCoord).rgb, 1.0);
		} else if (selection == 3) {
			sphereColor = vec4(texture(sphereTextureLB, tCoord).rgb, 1.0);
		} else if (selection == 4) {
			sphereColor = vec4(texture(sphereTextureRB, tCoord).rgb, 1.0);
		}
		
		atmColor  = vec4(texture(atmTexture, tCoord).rgb, 1.0);
	}
    
    vec4 color = mix(sphereColor * sphereBrightness, atmColor * atmBrightness, 0.5); 
    
    fragColor = vec4(color.rgb * overallBrightness, 1.0);
}
