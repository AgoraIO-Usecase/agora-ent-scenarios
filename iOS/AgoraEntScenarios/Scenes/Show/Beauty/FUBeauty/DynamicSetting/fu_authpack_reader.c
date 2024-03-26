//
//  fu_authpack_reader.c
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

#include "fu_authpack_reader.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* parse_fu_auth_pack(const char *file_path, int *length) {
    FILE* file = fopen(file_path, "r");
    if (file == NULL) {
        printf("无法打开文件\n");
        return NULL;
    }

    fseek(file, 0, SEEK_END);
    long fileSize = ftell(file);
    fseek(file, 0, SEEK_SET);

    char* fileContent = (char*)malloc(fileSize + 1);
    fread(fileContent, 1, fileSize, file);
    fileContent[fileSize] = '\0';

    // 寻找大括号内的整数数组
    char* start = strchr(fileContent, '{');
    if (!start) {
        printf("未找到'{'\n");
        free(fileContent);
        fclose(file);
        return NULL;
    }
    start++;

    char* end = strchr(start, '}');
    if (!end) {
        printf("未找到'}'\n");
        free(fileContent);
        fclose(file);
        return NULL;
    }

    int arrayLength = end - start;

    int* intArray = (int*)malloc(arrayLength * sizeof(int));
    int count = 0;

    char* token = strtok(start, " ,");
    while (token != NULL) {
        intArray[count++] = atoi(token);
        token = strtok(NULL, " ,");
    }

    char* charArray = (char*)malloc(arrayLength * sizeof(char));
    for (int i = 0; i < arrayLength; i++) {
        charArray[i] = (char)intArray[i];
    }

    *length = count;

    free(fileContent);
    free(intArray);
    fclose(file);

    return charArray;
}
