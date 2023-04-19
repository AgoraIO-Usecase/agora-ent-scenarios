package io.agora.scene.voice.spatial.service;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u00c4\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b)\n\u0002\u0010\u0000\n\u0002\b \u0018\u00002\u00020\u0001B+\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u001c\u0010\u0004\u001a\u0018\u0012\f\u0012\n\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\tJJ\u00104\u001a\u00020\b2\u0006\u00105\u001a\u00020\r28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JB\u0010<\u001a\u00020\b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JH\u0010=\u001a\u00020\b2\u0006\u00105\u001a\u00020\r26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016J^\u0010>\u001a\u00020\b2\u0006\u0010?\u001a\u00020\u000b2\u0006\u0010@\u001a\u00020\u000b2D\u00106\u001a@\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012!\u0012\u001f\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u0019\u0018\u00010A\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JH\u0010B\u001a\u00020\b2\u0006\u0010C\u001a\u00020D26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110%\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JF\u0010E\u001a\u00020\b2<\u00106\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u0002020F\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010G\u001a\u00020\b2\u0006\u0010H\u001a\u00020%28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010I\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JN\u0010J\u001a\u00020\b2\u0006\u0010K\u001a\u00020\u000b2<\u00106\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020%0F\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JF\u0010L\u001a\u00020\b2<\u00106\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u0002020F\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010M\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016J\u000e\u0010O\u001a\b\u0012\u0004\u0012\u00020(0\'H\u0016J\u0016\u0010P\u001a\u00020\b2\f\u0010Q\u001a\b\u0012\u0004\u0012\u00020\b0,H\u0002JA\u0010R\u001a\u00020\b2\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\b0,2)\u0010:\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(T\u0012\u0004\u0012\u00020\b0\u0005H\u0002J;\u0010U\u001a\u00020\b2\u0006\u0010V\u001a\u00020\u00192)\u00106\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0004\u0012\u00020\b0\u0005H\u0002J<\u0010W\u001a\u00020\b2\u0006\u0010X\u001a\u0002022\u0012\u0010S\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\b0\u00052\u0016\u0010:\u001a\u0012\u0012\b\u0012\u00060\u0006j\u0002`\u0007\u0012\u0004\u0012\u00020\b0\u0005H\u0002JN\u0010Y\u001a\u00020\b2D\u00106\u001a@\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\u00190F\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(Z\u0012\u0004\u0012\u00020\b07H\u0002JH\u0010[\u001a\u00020\b2\u0006\u0010\\\u001a\u00020\u001626\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0002J3\u0010]\u001a\u00020\b2)\u00106\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0004\u0012\u00020\b0\u0005H\u0002J\u0018\u0010^\u001a\u00020\u00192\u0006\u0010_\u001a\u00020\u000b2\u0006\u0010`\u001a\u00020\rH\u0002JF\u0010a\u001a\u00020\b2<\u00106\u001a8\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\u00160F\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0002J\"\u0010b\u001a\u00020\b2\u0018\u0010S\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190F\u0012\u0004\u0012\u00020\b0\u0005H\u0002J\u001c\u0010c\u001a\u00020\b2\u0012\u0010S\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\b0\u0005H\u0002JM\u0010d\u001a\u00020\b2\u0018\u0010S\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u0002020F\u0012\u0004\u0012\u00020\b0\u00052)\u0010:\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(T\u0012\u0004\u0012\u00020\b0\u0005H\u0002JA\u0010e\u001a\u00020\b2\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\b0,2)\u0010:\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(T\u0012\u0004\u0012\u00020\b0\u0005H\u0002JP\u0010f\u001a\u00020\b2\u0006\u0010g\u001a\u00020\r2\u0006\u0010\\\u001a\u00020\u001626\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0002J6\u0010h\u001a\u00020\b2\u0006\u00105\u001a\u00020\r2\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\b0,2\u0016\u0010:\u001a\u0012\u0012\b\u0012\u00060\u0006j\u0002`\u0007\u0012\u0004\u0012\u00020\b0\u0005H\u0002J\u0016\u0010i\u001a\u00020\b2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0,H\u0002J\u0016\u0010j\u001a\u00020\b2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0,H\u0002J\b\u0010k\u001a\u00020\bH\u0002J\u0016\u0010l\u001a\u00020\b2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0,H\u0002J\u0016\u0010m\u001a\u00020\b2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0,H\u0002J;\u0010n\u001a\u00020\b2\u0006\u0010o\u001a\u00020!2)\u00106\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0004\u0012\u00020\b0\u0005H\u0002JA\u0010p\u001a\u00020\b2\u0006\u0010q\u001a\u00020%2\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\b0,2!\u0010:\u001a\u001d\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0004\u0012\u00020\b0\u0005H\u0002JG\u0010p\u001a\u00020\b2\u0012\u0010r\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020s0A2)\u00106\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0004\u0012\u00020\b0\u0005H\u0002J;\u0010t\u001a\u00020\b2\u0006\u0010V\u001a\u00020\u00192)\u00106\u001a%\u0012\u001b\u0012\u0019\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0004\u0012\u00020\b0\u0005H\u0002J6\u0010u\u001a\u00020\b2\u0006\u0010X\u001a\u0002022\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\b0,2\u0016\u0010:\u001a\u0012\u0012\b\u0012\u00060\u0006j\u0002`\u0007\u0012\u0004\u0012\u00020\b0\u0005H\u0002JJ\u0010v\u001a\u00020\b2\u0006\u0010w\u001a\u00020\r28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010%\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010x\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010y\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JP\u0010z\u001a\u00020\b2\u0006\u0010w\u001a\u00020\r2\u0006\u0010{\u001a\u00020.26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010|\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010}\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016J@\u0010~\u001a\u00020\b26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016J\b\u0010\u007f\u001a\u00020\bH\u0016J\u001c\u0010\u0080\u0001\u001a\u00020\b2\u0006\u0010w\u001a\u00020\r2\t\b\u0002\u0010\u0081\u0001\u001a\u00020.H\u0002J\u0011\u0010\u0082\u0001\u001a\u00020\u000b2\u0006\u0010_\u001a\u00020\u000bH\u0002JQ\u0010\u0083\u0001\u001a\u00020\b2\b\u0010N\u001a\u0004\u0018\u00010\u000b26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016\u00a2\u0006\u0003\u0010\u0084\u0001JY\u0010\u0085\u0001\u001a\u00020\b2\u0006\u00105\u001a\u00020\r2\b\u0010N\u001a\u0004\u0018\u00010\u000b26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016\u00a2\u0006\u0003\u0010\u0086\u0001J\u0012\u0010\u0087\u0001\u001a\u00020\b2\u0007\u0010\u0088\u0001\u001a\u00020(H\u0016J\u0018\u0010\u0089\u0001\u001a\u00020\b2\r\u0010\u008a\u0001\u001a\b\u0012\u0004\u0012\u00020\b0,H\u0016JK\u0010\u008b\u0001\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JK\u0010\u008c\u0001\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JK\u0010\u008d\u0001\u001a\u00020\b2\u0006\u0010N\u001a\u00020\u000b28\u00106\u001a4\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0015\u0012\u0013\u0018\u00010\u0019\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016J\t\u0010\u008e\u0001\u001a\u00020\bH\u0016JJ\u0010\u008f\u0001\u001a\u00020\b2\u0007\u0010\u0090\u0001\u001a\u00020\r26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016JJ\u0010\u0091\u0001\u001a\u00020\b2\u0007\u0010\u0092\u0001\u001a\u00020!26\u00106\u001a2\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(:\u0012\u0013\u0012\u00110.\u00a2\u0006\f\b8\u0012\b\b9\u0012\u0004\b\b(;\u0012\u0004\u0012\u00020\b07H\u0016R\u000e\u0010\n\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R$\u0010\u0004\u001a\u0018\u0012\f\u0012\n\u0018\u00010\u0006j\u0004\u0018\u0001`\u0007\u0012\u0004\u0012\u00020\b\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\r0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\r0\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\r0\u001eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\r0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020!X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020#X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020%0\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020(0\'X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010)\u001a\b\u0012\u0004\u0012\u00020*0\'X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010+\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u000200X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u00101\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u0002020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00103\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0093\u0001"}, d2 = {"Lio/agora/scene/voice/spatial/service/VoiceSyncManagerServiceImp;", "Lio/agora/scene/voice/spatial/service/VoiceServiceProtocol;", "context", "Landroid/content/Context;", "errorHandler", "Lkotlin/Function1;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "", "(Landroid/content/Context;Lkotlin/jvm/functions/Function1;)V", "ROOM_AVAILABLE_DURATION", "", "currRoomNo", "", "kCollectionIdRobotInfo", "kCollectionIdSeatApply", "kCollectionIdSeatInfo", "kCollectionIdUser", "mSceneReference", "Lio/agora/syncmanager/rtm/SceneReference;", "micSeatApplyList", "Ljava/util/ArrayList;", "Lio/agora/scene/voice/spatial/model/VoiceRoomApply;", "micSeatMap", "", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "objIdOfRobotInfo", "objIdOfRoomNo", "objIdOfSeatApply", "objIdOfSeatInfo", "Ljava/util/HashMap;", "objIdOfUserId", "robotInfo", "Lio/agora/scene/voice/spatial/model/RobotSpatialAudioModel;", "roomChecker", "Lio/agora/scene/voice/spatial/service/RoomChecker;", "roomMap", "Lio/agora/scene/voice/spatial/model/VoiceRoomModel;", "roomServiceSubscribeDelegates", "", "Lio/agora/scene/voice/spatial/service/VoiceRoomSubscribeDelegate;", "roomSubscribeListener", "Lio/agora/syncmanager/rtm/Sync$EventListener;", "roomTimeUpSubscriber", "Lkotlin/Function0;", "syncUtilsInit", "", "timerRoomEndRun", "Ljava/lang/Runnable;", "userMap", "Lio/agora/scene/voice/spatial/model/VoiceMemberModel;", "voiceSceneId", "acceptMicSeatApply", "userId", "completion", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "error", "result", "acceptMicSeatInvitation", "cancelMicSeatApply", "changeMic", "oldIndex", "newIndex", "", "createRoom", "inputModel", "Lio/agora/scene/voice/spatial/model/VoiceCreateRoomModel;", "fetchApplicantsList", "", "fetchRoomDetail", "voiceRoomModel", "Lio/agora/scene/voice/spatial/model/VoiceRoomInfo;", "fetchRoomList", "page", "fetchRoomMembers", "forbidMic", "micIndex", "getSubscribeDelegates", "initScene", "complete", "innerAddRobotInfo", "success", "e", "innerAddSeatInfo", "seatInfo", "innerAddUser", "user", "innerAutoOnSeatIfNeed", "seat", "innerCreateMicSeatApply", "apply", "innerGenerateAllDefaultSeatInfo", "innerGenerateDefaultSeatInfo", "index", "uid", "innerGetAllMicSeatApply", "innerGetAllSeatInfo", "innerGetRobotInfo", "innerGetUserList", "innerMayAddLocalUser", "innerRemoveMicSeatApply", "objId", "innerRemoveUser", "innerSubscribeOnlineUsers", "innerSubscribeRobotInfo", "innerSubscribeRoomChanged", "innerSubscribeSeatApply", "innerSubscribeSeats", "innerUpdateRobotInfo", "robotSpatialAudioInfo", "innerUpdateRoomInfo", "curRoomInfo", "data", "", "innerUpdateSeat", "innerUpdateUserRoomRequestStatus", "joinRoom", "roomId", "kickOff", "leaveMic", "leaveRoom", "isRoomOwnerLeave", "lockMic", "muteLocal", "refuseInvite", "reset", "resetCacheInfo", "isRoomDestroyed", "selectEmptySeat", "startMicSeatApply", "(Ljava/lang/Integer;Lkotlin/jvm/functions/Function2;)V", "startMicSeatInvitation", "(Ljava/lang/String;Ljava/lang/Integer;Lkotlin/jvm/functions/Function2;)V", "subscribeEvent", "delegate", "subscribeRoomTimeUp", "onRoomTimeUp", "unForbidMic", "unLockMic", "unMuteLocal", "unsubscribeEvent", "updateAnnouncement", "content", "updateRobotInfo", "info", "voice_spatial_release"})
public final class VoiceSyncManagerServiceImp implements io.agora.scene.voice.spatial.service.VoiceServiceProtocol {
    private final android.content.Context context = null;
    private final kotlin.jvm.functions.Function1<java.lang.Exception, kotlin.Unit> errorHandler = null;
    private final java.lang.String voiceSceneId = "scene_spatialChatRoom";
    private final java.lang.String kCollectionIdUser = "user_collection";
    private final java.lang.String kCollectionIdSeatInfo = "seat_info_collection";
    private final java.lang.String kCollectionIdSeatApply = "show_seat_apply_collection";
    private final java.lang.String kCollectionIdRobotInfo = "robot_info_collection";
    private final io.agora.scene.voice.spatial.service.RoomChecker roomChecker = null;
    @kotlin.jvm.Volatile()
    private volatile boolean syncUtilsInit = false;
    private io.agora.syncmanager.rtm.SceneReference mSceneReference;
    @kotlin.jvm.Volatile()
    private volatile java.lang.String currRoomNo = "";
    private final java.util.Map<java.lang.String, io.agora.scene.voice.spatial.model.VoiceRoomModel> roomMap = null;
    private final java.util.Map<java.lang.String, java.lang.String> objIdOfRoomNo = null;
    private final java.util.List<io.agora.syncmanager.rtm.Sync.EventListener> roomSubscribeListener = null;
    private final java.util.List<io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate> roomServiceSubscribeDelegates = null;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.spatial.model.VoiceMemberModel> userMap = null;
    private final java.util.Map<java.lang.String, java.lang.String> objIdOfUserId = null;
    private final java.util.ArrayList<io.agora.scene.voice.spatial.model.VoiceRoomApply> micSeatApplyList = null;
    private final java.util.ArrayList<java.lang.String> objIdOfSeatApply = null;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> micSeatMap = null;
    private final java.util.HashMap<java.lang.Integer, java.lang.String> objIdOfSeatInfo = null;
    private io.agora.scene.voice.spatial.model.RobotSpatialAudioModel robotInfo;
    private java.lang.String objIdOfRobotInfo;
    private kotlin.jvm.functions.Function0<kotlin.Unit> roomTimeUpSubscriber;
    private final int ROOM_AVAILABLE_DURATION = 1200000;
    private final java.lang.Runnable timerRoomEndRun = null;
    
    public VoiceSyncManagerServiceImp(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> errorHandler) {
        super();
    }
    
    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    @java.lang.Override()
    public void subscribeEvent(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate delegate) {
    }
    
    /**
     * 取消订阅
     */
    @java.lang.Override()
    public void unsubscribeEvent() {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.util.List<io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate> getSubscribeDelegates() {
        return null;
    }
    
    @java.lang.Override()
    public void reset() {
    }
    
    /**
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     */
    @java.lang.Override()
    public void fetchRoomList(int page, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceRoomModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     */
    @java.lang.Override()
    public void createRoom(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceCreateRoomModel inputModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceRoomModel, kotlin.Unit> completion) {
    }
    
    /**
     * 加入房间
     * @param roomId 房间id
     */
    @java.lang.Override()
    public void joinRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceRoomModel, kotlin.Unit> completion) {
    }
    
    /**
     * 离开房间
     * @param roomId 房间id
     */
    @java.lang.Override()
    public void leaveRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, boolean isRoomOwnerLeave, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
     */
    @java.lang.Override()
    public void fetchRoomDetail(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.VoiceRoomModel voiceRoomModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceRoomInfo, kotlin.Unit> completion) {
    }
    
    /**
     * 获取用户列表
     */
    @java.lang.Override()
    public void fetchRoomMembers(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 申请列表
     */
    @java.lang.Override()
    public void fetchApplicantsList(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 申请上麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void startMicSeatApply(@org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 同意申请
     * @param userId 用户id
     */
    @java.lang.Override()
    public void acceptMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 取消上麦
     * @param userId im uid
     */
    @java.lang.Override()
    public void cancelMicSeatApply(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 邀请用户上麦
     * @param userId im uid
     */
    @java.lang.Override()
    public void startMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 接受邀请
     */
    @java.lang.Override()
    public void acceptMicSeatInvitation(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 拒绝邀请
     */
    @java.lang.Override()
    public void refuseInvite(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * mute
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void muteLocal(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * unMute
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void unMuteLocal(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 禁言指定麦位置
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void forbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void unForbidMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 锁麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void lockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 取消锁麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void unLockMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 踢用户下麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void kickOff(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 下麦
     * @param micIndex 麦位index
     */
    @java.lang.Override()
    public void leaveMic(int micIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super io.agora.scene.voice.spatial.model.VoiceMicInfoModel, kotlin.Unit> completion) {
    }
    
    /**
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    @java.lang.Override()
    public void changeMic(int oldIndex, int newIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel>, kotlin.Unit> completion) {
    }
    
    /**
     * 更新公告
     * @param content 公告内容
     */
    @java.lang.Override()
    public void updateAnnouncement(@org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    /**
     * 更新机器人配置
     * @param info 机器人配置
     */
    @java.lang.Override()
    public void updateRobotInfo(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.RobotSpatialAudioModel info, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    @java.lang.Override()
    public void subscribeRoomTimeUp(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRoomTimeUp) {
    }
    
    private final void initScene(kotlin.jvm.functions.Function0<kotlin.Unit> complete) {
    }
    
    private final void resetCacheInfo(java.lang.String roomId, boolean isRoomDestroyed) {
    }
    
    private final void innerUpdateRoomInfo(io.agora.scene.voice.spatial.model.VoiceRoomModel curRoomInfo, kotlin.jvm.functions.Function0<kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> error) {
    }
    
    private final void innerMayAddLocalUser(kotlin.jvm.functions.Function0<kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> error) {
    }
    
    private final void innerGetUserList(kotlin.jvm.functions.Function1<? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMemberModel>, kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> error) {
    }
    
    private final void innerAddUser(io.agora.scene.voice.spatial.model.VoiceMemberModel user, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> error) {
    }
    
    private final void innerRemoveUser(java.lang.String userId, kotlin.jvm.functions.Function0<kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> error) {
    }
    
    private final void innerUpdateUserRoomRequestStatus(io.agora.scene.voice.spatial.model.VoiceMemberModel user, kotlin.jvm.functions.Function0<kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> error) {
    }
    
    private final void innerSubscribeOnlineUsers(kotlin.jvm.functions.Function0<kotlin.Unit> completion) {
    }
    
    private final int selectEmptySeat(int index) {
        return 0;
    }
    
    private final void innerGetAllMicSeatApply(kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceRoomApply>, kotlin.Unit> completion) {
    }
    
    private final void innerCreateMicSeatApply(io.agora.scene.voice.spatial.model.VoiceRoomApply apply, kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    private final void innerRemoveMicSeatApply(java.lang.String objId, io.agora.scene.voice.spatial.model.VoiceRoomApply apply, kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super java.lang.Boolean, kotlin.Unit> completion) {
    }
    
    private final void innerSubscribeSeatApply(kotlin.jvm.functions.Function0<kotlin.Unit> completion) {
    }
    
    private final io.agora.scene.voice.spatial.model.VoiceMicInfoModel innerGenerateDefaultSeatInfo(int index, java.lang.String uid) {
        return null;
    }
    
    private final void innerGenerateAllDefaultSeatInfo(kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> completion) {
    }
    
    private final void innerGetAllSeatInfo(kotlin.jvm.functions.Function1<? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>, kotlin.Unit> success) {
    }
    
    private final void innerAutoOnSeatIfNeed(kotlin.jvm.functions.Function2<? super java.lang.Exception, ? super java.util.List<io.agora.scene.voice.spatial.model.VoiceMicInfoModel>, kotlin.Unit> completion) {
    }
    
    private final void innerUpdateSeat(io.agora.scene.voice.spatial.model.VoiceMicInfoModel seatInfo, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> completion) {
    }
    
    private final void innerAddSeatInfo(io.agora.scene.voice.spatial.model.VoiceMicInfoModel seatInfo, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> completion) {
    }
    
    private final void innerSubscribeSeats(kotlin.jvm.functions.Function0<kotlin.Unit> completion) {
    }
    
    private final void innerAddRobotInfo(kotlin.jvm.functions.Function0<kotlin.Unit> success, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> error) {
    }
    
    private final void innerGetRobotInfo(kotlin.jvm.functions.Function1<? super io.agora.scene.voice.spatial.model.RobotSpatialAudioModel, kotlin.Unit> success) {
    }
    
    private final void innerUpdateRobotInfo(io.agora.scene.voice.spatial.model.RobotSpatialAudioModel robotSpatialAudioInfo, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> completion) {
    }
    
    private final void innerSubscribeRobotInfo(kotlin.jvm.functions.Function0<kotlin.Unit> completion) {
    }
    
    private final void innerUpdateRoomInfo(java.util.Map<java.lang.String, ? extends java.lang.Object> data, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> completion) {
    }
    
    private final void innerSubscribeRoomChanged() {
    }
}