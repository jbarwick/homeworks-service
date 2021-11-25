package com.jvj28.homeworks.util;

import com.jvj28.homeworks.command.HomeworksCommand;

import java.util.concurrent.Future;

public interface Promise<E> extends Future<E> {

    Promise<E> onComplete(PromiseCallback<E> callback);

    void markComplete();

    E getCommand();

}
