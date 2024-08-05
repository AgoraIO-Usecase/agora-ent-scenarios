import os, sys

def modfiy(path, isReset):
    appId = os.environ.get('APP_ID')
    im_app_key = os.environ.get('IM_APP_KEY')
    dyna_res_key = os.environ.get('DYNAMIC_RESOURCE_URL')
    with open(path, 'r', encoding='utf-8') as file:
        contents = []
        for num, line in enumerate(file):
            line = line.strip()
            if "static let AppId" in line:
                if isReset:
                    line = "static let AppId: String = <#YOUR APPID#>"
                else:
                    print(f'replace line to: [static let AppId: String = "{appId}"]')
                    line = f'static let AppId: String = "{appId}"'
            
            elif "static var IMAppKey" in line:
                if isReset:
                    line = "static var IMAppKey: String? = <#YOUR IMAppKey#>"
                else:
                    value = im_app_key if len(im_app_key) > 0 else 'nil'
                    print(f'replace line to: [static var IMAppKey: String? = "{value}"]')
                    line = f'static var IMAppKey: String? = "{value}"'
                    
            elif "static var DynamicResourceUrl" in line:
                if isReset:
                    line = "static var DynamicResourceUrl: String? = "'nil'"
                else:
                    value = dyna_res_key if len(dyna_res_key) > 0 else 'nil'
                    print(f'replace line to: [static var DynamicResourceUrl: String = "{value}"]')
                    line = f'static var DynamicResourceUrl: String? = "{value}"'

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
    isReset = eval(sys.argv[1:][1])
    modfiy(path.strip(), isReset)
