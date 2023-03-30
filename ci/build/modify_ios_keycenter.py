import os, sys

def modfiy(path, isReset):
    appId = os.environ.get('APP_ID')
    cert = os.environ.get('APP_CERT')
    im_app_key = os.environ.get('IM_APP_KEY')
    im_client_id = os.environ.get('IM_CLIENT_ID')
    im_client_secret = os.environ.get('IM_CLIENT_SECRET')
    with open(path, 'r', encoding='utf-8') as file:
        contents = []
        for num, line in enumerate(file):
            line = line.strip()
            if "static let AppId" in line:
                if isReset:
                    line = "static let AppId: String = <#YOUR APPID#>"
                else:
                    line = f'static let AppId: String = "{appId}"'
            elif "static let Certificate" in line:
                if isReset:
                    line = "static let Certificate: String? = <#YOUR Certificate#>"
                else:
                    value = cert if len(cert) > 0 else 'nil'
                    line = f'static let Certificate: String? = "{value}"'

            elif "static var IMAppKey" in line:
                if isReset:
                    line = "static var IMAppKey: String? = <#YOUR IMAppKey#>"
                else:
                    value = im_app_key if len(im_app_key) > 0 else 'nil'
                    line = f'static var IMAppKey: String? = "{value}"'

            elif "static var IMClientId" in line:
                if isReset:
                    line = "static var IMClientId: String? = <#YOUR IMClientId#>"
                else:
                    value = im_client_id if len(im_client_id) > 0 else 'nil'
                    line = f'static var IMClientId: String? = "{value}"'

            elif "static var IMClientSecret" in line:
                if isReset:
                    line = "static var IMClientSecret: String? = <#YOUR IMClientSecret#>"
                else:
                    value = im_client_secret if len(im_client_secret) > 0 else 'nil'
                    line = f'static var IMClientSecret: String? = "{value}"'

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
