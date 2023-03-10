#coding=utf-8
import os, sys

def modfiy(path, beauty_type):
    with open(path, 'r', encoding='utf-8') as file:
        contents = []
        for num, line in enumerate(file):
            line = line.strip()
            if beauty_type == '字节' and 'bytedEffect' in line:
                line = "pod 'bytedEffect', :path => 'bytedEffect.podspec'"

            elif beauty_type == '相芯' and 'fuLib' in line:
                line = "pod 'fuLib', :path => 'fu.podspec'"

            elif beauty_type == '商汤' and 'senseLib' in line:
                line = "pod 'senseLib', :path => 'sense.podspec'"

            contents.append(line)
        file.close()
        
        with open(path, 'w', encoding='utf-8') as fw:
            for content in contents:
                if "{" in content or "}" in content:
                    fw.write(content + "\n")
                else:
                    fw.write('\t'+content + "\n")
            fw.close()


if __name__ == '__main__':
    print(f'argv === {sys.argv[1:]}')
    path = sys.argv[1:][0]
    beauty_type = eval(sys.argv[1:][1])
    modfiy(path.strip(), beauty_type)
