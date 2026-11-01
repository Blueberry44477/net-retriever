package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Abstract Colleague in the GoF Mediator pattern.
 * Keeps a reference to the {@link DownloadSessionMediator} and communicates with peer colleagues
 * exclusively through mediator event dispatches.
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class DownloadColleague {

    @Getter
    @Setter
    protected DownloadSessionMediator mediator;

    /**
     * Unique identifier or role name of this colleague.
     *
     * @return colleague name
     */
    public abstract String getName();
}
